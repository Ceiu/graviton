package com.redhat.graviton.certs;

import com.redhat.graviton.api.candlepin.cert.*;
import com.redhat.graviton.db.model.*;
import com.redhat.graviton.db.curators.ProductCurator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.jboss.logging.Logger;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.nio.charset.StandardCharsets;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.zip.DeflaterOutputStream;



@Dependent
public class SCACertificateGenerator {


    private final CertificateAuthority certAuthority;
    private final KeyGenerator keyGenerator;
    private final Provider<X509CertificateBuilder> certBuilderProvider;
    private final PKIUtility pkiUtil;

    private final ProductCurator productCurator;
    private final ObjectMapper mapper;




    @Inject
    public SCACertificateGenerator(CertificateAuthority certAuthority, KeyGenerator keyGenerator,
        Provider<X509CertificateBuilder> certBuilderProvider, PKIUtility pkiUtil,
        ProductCurator productCurator, ObjectMapper mapper) {

        this.certAuthority = Objects.requireNonNull(certAuthority);
        this.keyGenerator = Objects.requireNonNull(keyGenerator);
        this.certBuilderProvider = Objects.requireNonNull(certBuilderProvider);
        this.pkiUtil = Objects.requireNonNull(pkiUtil);
        this.productCurator = Objects.requireNonNull(productCurator);
        this.mapper = Objects.requireNonNull(mapper);
    }


    public SCAContentCertificate generateSCACertificate(Organization organization, Consumer consumer) {
        if (organization == null) {
            throw new IllegalArgumentException("organization is null");
        }

        if (consumer == null) {
            throw new IllegalArgumentException("consumer is null");
        }

        String filter = this.buildSCAFilterString(organization, consumer);

        Instant validAfter = Instant.now();
        Instant validUntil = validAfter.plus(1 * 365, ChronoUnit.DAYS);

        // generate x509 certificate
        KeyPair keypair = this.keyGenerator.generateKeyPair();
        X509Certificate x509cert = this.buildX509Certificate(organization, consumer, keypair,
            validAfter, validUntil);

        // generate content blob
        String scaContentData = this.buildSCAContentData(organization, consumer);

        return new SCAContentCertificate()
            .setFilter(filter)
            .setSerialNumber(x509cert.getSerialNumber().longValue())
            .setValidAfter(validAfter)
            .setValidUntil(validUntil)
            .setPrivateKey(this.pkiUtil.toPemEncodedString(keypair.getPrivate()))
            .setCertificate(this.pkiUtil.toPemEncodedString(x509cert))
            .setContentData(scaContentData);
    }

    private X509Certificate buildX509Certificate(Organization org, Consumer consumer, KeyPair keypair,
        Instant validAfter, Instant validUntil) {

        String distinguishedName = String.format("CN=%s, O=%s", consumer.getOid(), org.getOid());

        // This is a lot of object juggling just to build a simple string, which we then throw at a strange
        // huffman encoder...
        // CertContent certContent = new CertContent()
        //     .setPath(String.format("/sca/%s", org.getOid()));

        // CertProduct certProduct = new CertProduct()
        //     .setContent(List.of(certContent));

        // CertEntitlement certEntitlement = new CertEntitlement()
        //     .setProducts(List.of(certProduct));

        return this.certBuilderProvider.get()
            .generateCertificateSerial()
            .setDistinguishedName(distinguishedName)
            .setValidAfter(validAfter)
            .setValidUntil(validUntil)
            .setKeyPair(keypair)

            // Magic Red Hat extension OID based on CP code
            .addExtension(new X509StringExtension("1.3.6.1.4.1.2312.9.6", false, "3.4"))

            // General CP entitlement content extension (also present in SCA certs)
            // Output should be a huffman code for the gzipped string segments (path: /sca/org.oid => sca, org.oid)
            // This requires a, frankly, strange huffman encoder to implement, and that will take more time
            // than I'm willing to spend of the time allotted to work on this
            // .addExtension()

            .build();
    }

    private String buildSCAContentData(Organization org, Consumer consumer) {
        try {
            Map<String, Content> contentMap = this.productCurator.getOrgContent(org.getOid(), true);
            SortedSet<String> consumerArches = this.getArchesOf(consumer);
            this.filterContent(contentMap, consumerArches);

            // Convert to cert model JSON
            CertProduct certProduct = this.buildCertProduct(contentMap);
            CertEntitlement certEntitlement = this.buildCertEntitlement(consumer, certProduct);
            String json = this.mapper.writeValueAsString(certEntitlement);

            // base64 encode the payload and return it as a string
            String b64compressed = this.compressAndEncodeJson(json);

            return b64compressed;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Arch filtering junk
    private static final Set<String> GLOBAL_ARCHES = Set.of("all", "ALL", "noarch");
    private static final Set<String> X86_LABELS = Set.of("i386", "i486", "i586", "i686");

    private void filterContent(Map<String, Content> contentMap, Collection<String> archFilter) {
        if (archFilter == null || archFilter.isEmpty()) {
            return;
        }

        Iterator<Map.Entry<String, Content>> contentIterator = contentMap.entrySet().iterator();
        while (contentIterator.hasNext()) {
            Map.Entry<String, Content> entry = contentIterator.next();

            if (!this.archesMatch(archFilter, entry.getValue())) {
                contentIterator.remove();
            }
        }
    }

    private Set<String> parseArches(String arches) {
        Set<String> parsed = new HashSet<>();

        if (arches == null || arches.trim().isEmpty()) {
            return parsed;
        }

        for (String arch : arches.trim().split("\\s*,[\\s,]*")) {
            parsed.add(arch);
        }

        return parsed;
    }

    private boolean archesMatch(Collection<String> archFilter, Content content) {
        Set<String> contentArches = this.parseArches(content.getArches());

        // No arch specified, auto-match?
        if (contentArches.isEmpty()) {
            return true;
        }

        // Check the arch filter for any matches
        for (String contentArch : contentArches) {
            if (GLOBAL_ARCHES.contains(contentArch)) {
                return true;
            }

            for (String arch : archFilter) {
                if (arch.equals(contentArch)) {
                    return true;
                }

                if (X86_LABELS.contains(arch) && contentArch.equals("x86")) {
                    return true;
                }
            }
        }

        return false;
    }

    private CertProduct buildCertProduct(Map<String, Content> contentMap) {

        // When it comes to filtering content, there are a lot of bits CP does that are
        // implicitly ignored by way of the product pass into the X509ExtensionUtil being a
        // minimialist product. This is probably not good (tm) long term, but makes life easier
        // for me here.
        List<CertContent> certContent = contentMap.values()
            .stream()
            .map(content -> this.convertToCertContent(content))
            .collect(Collectors.toList());

        CertProduct certProduct = new CertProduct()
            .setId("simple_content_access")
            .setName("Simple Content Access")
            .setContent(certContent)

            // sca does not define version
            .setVersion("")

            // sca does not define product branding
            .setBrandName(null)
            .setBrandType(null)

            // Amusingly, SCA cert payloads don't define arches, even though they're filtered by
            // arch. We'll leave it null for now but this shouldn't be left empty in the future.
            // We can do better, and we should.
            .setArchitectures(List.of());

        return certProduct;
    }

    private CertContent convertToCertContent(Content content) {
        CertContent certContent = new CertContent()
            .setId(content.getOid())
            .setType(content.getType())
            .setName(content.getName())
            .setLabel(content.getLabel())
            .setVendor(content.getVendor())
            .setGpgUrl(content.getGpgUrl())
            .setMetadataExpiration(content.getMetadataExpiration())

            // In CP proper, this actually goes through a bunch of checks, but for SCA certs, the
            // sku product has no attributes and, thus, no content enablement overrides. Since we
            // also don't have environments to deal with, this means we just copy it over 1:1.
            .setEnabled(content.isEnabled());

        // Include required tags as a list if present...
        String requiredTags = content.getRequiredTags();
        if (requiredTags != null) {
            List<String> parsedTags = List.of(requiredTags.trim().split("\\s*,[\\s,]*"));
            certContent.setRequiredTags(parsedTags);
        }

        return certContent;
    }

    private CertEntitlement buildCertEntitlement(Consumer consumer, CertProduct certProduct) {
        CertEntitlement certEntitlement = new CertEntitlement()
            .setConsumer(consumer.getId())
            .setProducts(List.of(certProduct))

            // SCA certs don't specify a quantity
            .setQuantity(null)

            // When building cert objects for SCA certs, CP uses a largely unpopulated dummy pool.
            // This means much of it will be nulled or empty population and there's no real value
            // in attempting to pass something through.
            .setSubscription(this.buildCertSubscription(certProduct))
            .setOrder(this.buildCertOrder())
            .setPool(this.buildCertPool(certProduct));

        return certEntitlement;
    }

    private CertSubscription buildCertSubscription(CertProduct certProduct) {
        CertSubscription certSubscription = new CertSubscription()
            .setSku(certProduct.getId())
            .setName(certProduct.getName());

        // The following properties never get set for an SCA cert, as the product has no attributes:
        // WARNING_PERIOD
        // SOCKETS
        // RAM
        // CORES
        // MANAGEMENT_ENABLED
        // STACKING_ID
        // VIRT_ONLY
        // USAGE
        // ROLES
        // ADDONS
        // SUPPORT_LEVEL
        // SUPPORT_TYPE

        return certSubscription;
    }

    private CertOrder buildCertOrder() {
        CertOrder certOrder = new CertOrder();

        // The following properties are never set for an SCA cert:
        // number
        // quantity
        // account number
        // contract number

        // The dates *are* set, but both the start *AND* end date are set to now...
        Instant start = Instant.now();
        Instant end = start; // start.plus(1, ChronoUnit.YEARS);

        // ...but they're set as strings. AAAAAARGH. Luckily it's an 8601 timestamp string, so this
        // is low-effort for us.
        certOrder.setStart(start.toString())
            .setEnd(end.toString());

        return certOrder;
    }

    private CertPool buildCertPool(CertProduct certProduct) {
        // We don't have a pool at all here, so we'll just steal the ID from the product. It's all
        // arbitrary anyway, so whatever I guess...

        CertPool certPool = new CertPool()
            .setId(certProduct.getId());

        return certPool;
    }

    private String compressAndEncodeJson(String json) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DeflaterOutputStream dos = new DeflaterOutputStream(baos);

        dos.write(json.getBytes(StandardCharsets.UTF_8));
        dos.finish();
        dos.close();

        byte[] compressed = baos.toByteArray();

        String b64encoded = Base64.getEncoder().encodeToString(compressed);
        byte[] signed = this.certAuthority.sign(compressed);

        return new StringBuilder()
            .append("-----BEGIN ENTITLEMENT DATA-----\n")
            .append(this.base64EncodeWithLineLimit(compressed, 64))
            .append("-----END ENTITLEMENT DATA-----\n")
            .append("-----BEGIN RSA SIGNATURE-----\n")
            .append(this.base64EncodeWithLineLimit(signed, 64))
            .append("-----END RSA SIGNATURE-----\n")
            .toString();
    }

    private String base64EncodeWithLineLimit(byte[] bytes, int lineLen) {

        String encoded = Base64.getEncoder().encodeToString(bytes);
        int length = encoded.length();

        StringBuilder builder = new StringBuilder();

        int offset = 0;
        while (offset + lineLen < length) {
            builder.append(encoded.substring(offset, offset + lineLen))
                .append('\n');

            offset += lineLen;
        }

        builder.append(encoded.substring(offset))
            .append('\n');

        return builder.toString();
    }





    private SortedSet<String> getArchesOf(Consumer consumer) {
        if (consumer == null) {
            throw new IllegalArgumentException("consumer is null");
        }

        SortedSet<String> arches = new TreeSet<>();

        Map<String, String> facts = consumer.getFacts();
        if (facts == null || facts.isEmpty()) {
            // If the consumer has no facts, we can't filter by any arch, so don't try
            return arches;
        }

        String supported = facts.get("supported_architectures");
        if (supported != null) {
            for (String arch : supported.trim().toLowerCase().split("\\s*,[\\s,]*")) {
                arches.add(arch);
            }
        }

        String arch = facts.get("uname.machine");
        if (arch != null && !arch.isEmpty()) {
            arches.add(arch.toLowerCase());
        }

        return arches;
    }

    public String buildSCAFilterString(Organization org, Consumer consumer) {
        if (org == null) {
            throw new IllegalArgumentException("org is null");
        }

        if (consumer == null) {
            throw new IllegalArgumentException("consumer is null");
        }

        SortedSet<String> arches = this.getArchesOf(consumer);
        String filter = !arches.isEmpty() ?
            String.format("org=%s;arches=%s", org.getOid(), String.join(",", arches)) :
            String.format("org=%s", org.getOid());

        return filter;
    }


}
