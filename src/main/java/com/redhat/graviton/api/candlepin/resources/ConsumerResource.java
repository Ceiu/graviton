package com.redhat.graviton.api.candlepin.resources;

import com.redhat.graviton.api.candlepin.cert.*;
import com.redhat.graviton.api.candlepin.model.*;
import com.redhat.graviton.api.datasource.model.*;
import com.redhat.graviton.db.curators.*;
import com.redhat.graviton.db.model.*;
import com.redhat.graviton.sync.*;
import com.redhat.graviton.certs.CertificateAuthority;
import com.redhat.graviton.certs.KeyGenerator;
import com.redhat.graviton.certs.PKIUtility;
import com.redhat.graviton.certs.X509CertificateBuilder;
import com.redhat.graviton.certs.X509StringExtension;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.jboss.logging.Logger;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.MediaType;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.DeflaterOutputStream;
import java.util.stream.*;
import java.security.Signature;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;




@Path("/candlepin/consumers")
public class ConsumerResource {
    private static final Logger LOG = Logger.getLogger(ConsumerResource.class);

    @Inject
    private ObjectMapper mapper;

    @Inject
    private ConsumerCurator consumerCurator;

    @Inject
    private OrganizationCurator orgCurator;

    @Inject
    private ProductCurator productCurator;

    @Inject
    private CertificateAuthority certAuthority;

    @Inject
    private KeyGenerator keyGenerator;

    @Inject
    private Provider<X509CertificateBuilder> certBuilderProvider;

    @Inject
    private PKIUtility pkiUtil;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{consumer_oid}")
    public CPConsumerDTO getConsumer(@PathParam("consumer_oid") String consumerOid) {
        Consumer consumer = this.consumerCurator.getConsumerByOid(consumerOid);
        if (consumer == null) {
            LOG.errorf("no such consumer: %s", consumerOid);
            throw new NotFoundException("No such consumer: " + consumerOid);
        }

        return this.convertToConsumerDTO(consumer);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public CPConsumerDTO register(CPConsumerDTO dto,
        @QueryParam("username") String username,
        @QueryParam("owner") String orgOid,
        @QueryParam("activation_keys") String activationKeys,
        @QueryParam("identity_cert_creation") @DefaultValue("true") Boolean identityCertCreation) {

        if (dto == null) {
            throw new BadRequestException("no consumer provided??");
        }

        LOG.infof("Received new registration request for: %s, %s, %s", dto.getName(), username, orgOid);

        // If the user didn't specify an org, check the dto
        if (orgOid == null || orgOid.isEmpty()) {
            orgOid = dto.getOwner() != null ? dto.getOwner().getKey() : null;

            if (orgOid == null || orgOid.isEmpty()) {
                throw new BadRequestException("no organization specified");
            }
        }

        LOG.infof("Looking up org by oid: %s", orgOid);

        Organization org = this.orgCurator.getOrgByOid(orgOid);
        if (org == null) {
            LOG.errorf("No such organization: %s", orgOid);
            throw new NotFoundException("no such org: " + orgOid);
        }

        LOG.infof("Converting DTO to a real consumer");

        // convert dto to consumer
        Consumer consumer = this.convertToConsumer(org, dto, username);

        LOG.infof("Generating key pair for consumer %s", consumer);

        // create a keypair for this consumer
        KeyPairData kpdata = this.generateConsumerKeyPair(consumer);

        LOG.infof("Generated key pair for consumer: %s, %s", kpdata.getPublicKey(), kpdata.getPrivateKey());

        LOG.infof("Generating certificate for consumer %s", consumer);

        // create an identity cert for them
        Certificate certificate = this.generateIdentityCert(consumer);

        LOG.infof("Checking our work...");

        if (!kpdata.getPrivateKey().equals(certificate.getPrivateKey())) {
            LOG.errorf("Private key mismatch??\nkpdata.private_key:\n%s\n\ncertificate.private_key:\n%s",
                kpdata.getPrivateKey(), certificate.getPrivateKey());

            throw new RuntimeException("SOMEHOW THE PRIVATE KEYS ARE NOT EQUAL!");
        }

        LOG.infof("Persisting consumer data...");

        // persist
        this.consumerCurator.persist(kpdata);
        this.consumerCurator.persist(certificate);
        this.consumerCurator.persist(consumer);

        LOG.infof("Converting consumer back to a CP consumer DTO...");

        // convert consumer to dto & return
        return this.convertToConsumerDTO(consumer);
    }

    private KeyPairData generateConsumerKeyPair(Consumer consumer) {
        KeyPair keypair = this.keyGenerator.generateKeyPair();

        KeyPairData kpdata = new KeyPairData()
            .setPublicKey(this.pkiUtil.toPemEncodedString(keypair.getPublic()))
            .setPrivateKey(this.pkiUtil.toPemEncodedString(keypair.getPrivate()));

        consumer.setKeyPair(kpdata);

        return kpdata;
    }

    private Certificate generateIdentityCert(Consumer consumer) {
        KeyPairData kpdata = consumer.getKeyPair();
        if (kpdata == null) {
            throw new IllegalStateException("consumer does not yet have a key pair");
        }

        KeyPair keypair = this.pkiUtil.convertToKeyPair(kpdata);

        Instant validAfter = Instant.now();
        Instant validUntil = validAfter.plus(15 * 365, ChronoUnit.DAYS);

        X509Certificate x509cert = this.certBuilderProvider.get()
            .generateCertificateSerial()
            .setDistinguishedName("CN=" + consumer.getOid())
            .setSubjectAltName(consumer.getName())
            .setValidAfter(validAfter)
            .setValidUntil(validUntil)
            .setKeyPair(keypair)
            .build();

        Certificate cert = new Certificate()
            .setSerialNumber(x509cert.getSerialNumber().longValue())
            .setValidAfter(validAfter)
            .setValidUntil(validUntil)
            .setPrivateKey(this.pkiUtil.toPemEncodedString(keypair.getPrivate()))
            .setCertificate(this.pkiUtil.toPemEncodedString(x509cert));

        consumer.setCertificate(cert);

        return cert;
    }

    private Consumer convertToConsumer(Organization org, CPConsumerDTO dto, String username) {
        CPConsumerTypeDTO ctype = dto.getType();
        String type = ctype != null ? ctype.getLabel() : null;

        Instant now = Instant.now();

        Consumer consumer = new Consumer()
            .setOid(UUID.randomUUID().toString())
            .setName(dto.getName())
            .setType(type != null ? type : "SYSTEM")
            .setOrganization(org)
            .setUsername(username)
            .setFacts(dto.getFacts())
            .setLastCheckIn(now)
            .setLastCloudProfileUpdate(now);

        return consumer;
    }

    private CPConsumerDTO convertToConsumerDTO(Consumer consumer) {
        Instant now = Instant.now();

        CPConsumerTypeDTO type = new CPConsumerTypeDTO()
            .setCreated(now)
            .setUpdated(now)
            .setId(consumer.getType())
            .setLabel(consumer.getType())
            .setManifest(false);

        Organization org = consumer.getOrganization();
        CPOwnerDTO owner = null;
        if (org != null) {
            owner = new CPOwnerDTO()
                .setId(org.getId())
                .setKey(org.getOid())
                .setDisplayName(org.getName())
                .setContentAccessMode("org_environment");
        }

        Certificate certificate = consumer.getCertificate();
        CPCertificateDTO identityCert = null;
        if (certificate != null) {
            CPCertificateSerialDTO certSerial = new CPCertificateSerialDTO()
                .setCreated(certificate.getCreated())
                .setUpdated(certificate.getCreated())
                .setId(certificate.getSerialNumber())
                .setSerial(certificate.getSerialNumber()) // pointless legacy junk
                .setExpiration(certificate.getValidUntil())
                .setRevoked(false);

            identityCert = new CPCertificateDTO()
                .setId(certificate.getId())
                .setCreated(certificate.getCreated())
                .setUpdated(certificate.getCreated())
                .setKey(certificate.getPrivateKey())
                .setCert(certificate.getCertificate())
                .setSerial(certSerial);
        }

        CPConsumerDTO consumerDTO = new CPConsumerDTO()
            .setId(consumer.getId())
            .setCreated(consumer.getCreated())
            .setUpdated(consumer.getUpdated())
            .setUuid(consumer.getOid())
            .setName(consumer.getName())
            .setOwner(owner)
            .setUsername(consumer.getUsername())
            .setFacts(consumer.getFacts())
            .setIdCert(identityCert)
            .setHref("/candlepin/consumers/" + consumer.getOid())
            .setActivationKeys(List.of())
            .setEnvironments(List.of());

        return consumerDTO;
    }


// {
//   "lastUpdate" : "2023-06-05T02:46:21+0000",
//   "contentListing" : {
//     "528232839942281128" : [ "
//         -----BEGIN CERTIFICATE-----
//         MIIGAzCCA+ugAwIBAgIIB1So+B90l6gwDQYJKoZIhvcNAQELBQAwRDEcMBoGA1UE
//         AwwTQ2FuZGxlcGluIFNlcnZlciBDQTESMBAGA1UECwwJQ2FuZGxlcGluMRAwDgYD
//         VQQKDAdSZWQgSGF0MB4XDTIzMDYwNTAxNDYyMVoXDTI0MDYwNTAxNDYyMVowTTEc
//         MBoGA1UECgwTdGVzdF9vd25lci1BMUYxNzNCNjEtMCsGA1UEAwwkOGQ3YWUyZWUt
//         YzJmYy00NDZiLWJiMTAtMTA1Nzg0OTVkYWI2MIICIjANBgkqhkiG9w0BAQEFAAOC
//         Ag8AMIICCgKCAgEA5mxCl78BY0fgjFBkvGunqK4CryFHYfgCc/UOZx1/iDwk/Ugb
//         69lhj1/jB3YERMp96+gqNQy9fUGtvG0hee6Pzqde1UtsEDY6eh+SlaXPugCqmxVv
//         F7FUJhpWVMwUahjdaYFvXrT0J4d6QXRSAiqVtsn/9hg1Y8QGzlocUPzLC9FL3HId
//         xWdo0nG7nwrLHqOEnwNpLO9Rlt4Nsl54J35pnenN1bzxWXnFMG6ie9bvNh3+b5+v
//         HKPJKFt14nsw8u/scVXP2Ps0qBNgiC06AUfppPqI+k1fbr2ZmGBVac6JlaKDSCac
//         Ns0/j755UUXcyNEkDuYL4XtiY5rvLYcfzj6cQQZwNtPNwqSJVevf5jFEQuv5EKi+
//         vZkp2iv+z6278uWh2rWA9U8XUo7C73NiNnd9eYiEe9n5pnxocHpDBJUBep2hUCPn
//         lbFIWzsjLIV1fODRGdtKqPP3XDGc15rchWSQ7VuV0+GN6OlpBgFGLZf3AANPsZPp
//         1YazovQpM09gW0NOPO3hKrUg6pGXadPdbvoLzuDYPHS1YLLn8cn8M8zOffD5oOVm
//         FF9hJJdnGTuZzn14P81FjoNMBWQWcrytesgTcHYY9ndmE11kcUllu2T8eJTN5TBe
//         Y3wqIwNG11msVgMPaiB+HgdqJMCwJtEpt12sp+jfY1+vfPJbAdaoVWpI2gsCAwEA
//         AaOB7zCB7DAOBgNVHQ8BAf8EBAMCBLAwEwYDVR0lBAwwCgYIKwYBBQUHAwIwCQYD
//         VR0TBAIwADARBglghkgBhvhCAQEEBAMCBaAwHQYDVR0OBBYEFE4qJlxMCCDKcG7f
//         DvRFycTAG7/oMB8GA1UdIwQYMBaAFPfWFAIdJ+M3s3PaGri9qw9V+NyCMBIGCSsG
//         AQQBkggJBgQFDAMzLjQwFwYJKwYBBAGSCAkIBAoMCE9yZ0xldmVsMDoGCSsGAQQB
//         kggJBwQtBCt42itJLS6Jzy/PSy3SdTR0MzQ3djJjyEvMTdW1BLLMHd0MGADGEAnj
//         A6YAMA0GCSqGSIb3DQEBCwUAA4ICAQByjm/90LtxHrtXbwzd4NvnwMZ/CPBQaZEJ
//         dJWkDFa7PuxC76sJ1jHeX4h63nl2Y8WOH0S38Dr9zRzy4vca1R/cO31xU3GVDyi4
//         +1hk+sKldQVPUWmhGiNCMZN0I7062uz4pE3bEYGshTPTEyJlQPyB2NHXyuTY1mH/
//         7/55hh8ZEnw4OWXT2QNlFLn81nw9L/NWT5fY5kdSglX19f5X8xxnN7X18xCcpG9i
//         PGOaPnAAD0zzjlYNUS0KR9WrRIWEgUDTZ/L39GW1hWQbLwF7cVWvSIrJdxbOq9w6
//         bNIc09kQoTsMSbkKtYZ4lgRI0MpUarqpcW2G+vFe84d9PSPzV+jvW9c1jdxahFRT
//         Rk8KQBN0PoX/HryM7KG/v0W8EtkTJ844qginVfTXUwA9RJCdKfOMadkWTjACBV6/
//         wfv5O2RNOQTgyLEvVuieOLn0IEb6CVuMZUlEupAVpr8x+pJJPu2kGZ7GqB40PA9A
//         ZwDmlZpalAh7tWcDywb1pg8f/3YM8N00eaeaAA4jMNSphwLeVjwZ1p6k3eu6VRQi
//         TmeQO/T+hh/DyWO8wIhFJPtretZTyqsAykLL2+IKLNVN2MvMW1f0Yqatn0uAWoIJ
//         DbjP6pa0+3b1dhD6Gq9EsgPxlZt0SqTlHmaNojajTXGiM8oiXzJbkBXrmq/Wq2DJ
//         XSJRBDGHmA==
//         -----END CERTIFICATE-----
//     ", "
//         -----BEGIN ENTITLEMENT DATA-----
//         eJx9js1qwzAQhF/F7NkCWbUdV7fQV8gppQRptSGmiWT004vRu3ed0EshOc7szLez
//         Agafyo0iaJjczpAiEqjOKPp+tMLaTopODrupfx+csSO0kIpNGOclz8GDXiF9Fy4z
//         J5PPJ4NIKXHMmxux//Hwm/3Dry2E6LZ3XMwmZo4oqd6EHIUcDlLpftSqOzKAvHt2
//         ZMoSgyuYE+jPFWb3YkHzb0ILPxTTfTywMBEvcybMJdJG+2r/SJuoLJcQrry31l8G
//         qF5y
//         -----END ENTITLEMENT DATA-----
//         -----BEGIN RSA SIGNATURE-----
//         KAeeXAhspiRBSeJ+ENOiNfBoqdCQb01YFgkPJEpAo7mtQFE5vxjjeBjOW704yQBF
//         arKRmexj5Of/nRI2TLiWtFFWAQQNVaZgZyfKOKa23OoqObyANYml7QbkGJHL99Oo
//         zs4di1UO73RgyDxocpfLBcUwRs19rd6EuZqx+towxnKn9cyuTH72iFVG2VDLesgm
//         pcKviSFY7ohN37X5BkzGgDsn21tTLal0s/XewsgXku19jOPCi2WWT5aGbjBdqjXa
//         Tq7czmUxxgxUPl6fHZx17dFzsLcF/9Cs93ClT0gstWmpjpdCz8GtXYfRZDLhl/il
//         FTJdMWh64bEMOGX80ohy3aRfM9/85g5urBIx+sRzAyOFlOBZgBdrZjnQtofPLWiy
//         pK73QRf+BoIVX2RmGrlEVJpkxsZkEpbTk4gExhl09TJ59Y+htxGH08zirD+l0UEq
//         g3YIkidG3/r7/ki/2enuiYtZZBirMr9tWicJu/fLkC2Xoq/qlnQfHNijwsuQUBMF
//         ql7R2S+GpAfLZ3PFCmCWBt1TM9DBprKLExCXGGQOf3ySOp6Q2bZBcPSGJF968qST
//         YjM+HkFXBarVCxiyxgXCftC4uuKaYyXMW8IT8Kgz+mPLL5eWjfcoy+CgTqOz3ct2
//         aoKrpNeYrqKNXhT9I6Hh6aRgKe6BvOBNQarugpnPy60=
//         -----END RSA SIGNATURE-----
//     " ]
//   }
// }

    @GET
    @Path("/{consumer_oid}/accessible_content")
    @Produces(MediaType.APPLICATION_JSON)
    public CPContentAccessListing getContentAccessBody(
        @PathParam("consumer_oid") String consumerOid,
        @QueryParam("last_update") Instant lastUpdate,
        @QueryParam("arch") List<String> archFilter) {

        Consumer consumer = this.consumerCurator.getConsumerByOid(consumerOid);
        if (consumer == null) {
            LOG.errorf("no such consumer: %s", consumerOid);
            throw new NotFoundException("No such consumer: " + consumerOid);
        }

        // For now, use the consumer UUID as the org ID. They're effectively the same in this context anyway
        Organization org = consumer.getOrganization();
        LOG.infof("Using organization: %s", org);

        Map<String, Content> contentMap = this.productCurator.getOrgContent(org.getOid(), true);
        this.filterContent(contentMap, archFilter);

        Instant lastUpdated = Instant.now();
        Long certSerial = (long) (Math.random() * Long.MAX_VALUE);

        // This is profoundly silly. We rip apart a valid PEM-formatted cert + payload to turn it
        // into bad JSON!? WHY WHY WHY!?
        List<String> pieces = List.of(
            this.getSCACertData(org, consumer, contentMap),
            this.getSCAEntitlementData(org, consumer, contentMap)
        );

        CPContentAccessListing output = new CPContentAccessListing()
            .setContentListing(certSerial, pieces)
            .setLastUpdate(lastUpdated);

        return output;
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

    // SCA cert generation junk
    private String getSCACertData(Organization org, Consumer consumer, Map<String, Content> contentMap) {
        KeyPairData kpdata = consumer.getKeyPair();
        if (kpdata == null) {
            throw new IllegalStateException("consumer does not yet have a key pair");
        }

        KeyPair keypair = this.pkiUtil.convertToKeyPair(kpdata);

        Instant validAfter = Instant.now();
        Instant validUntil = validAfter.plus(1 * 365, ChronoUnit.DAYS);

        String distinguishedName = String.format("CN=%s, O=%s", consumer.getOid(), org.getOid());

        // This is a lot of object juggling just to build a simple string, which we then throw at a strange
        // huffman encoder...
        // CertContent certContent = new CertContent()
        //     .setPath(String.format("/sca/%s", org.getOid()));

        // CertProduct certProduct = new CertProduct()
        //     .setContent(List.of(certContent));

        // CertEntitlement certEntitlement = new CertEntitlement()
        //     .setProducts(List.of(certProduct));

        X509Certificate x509cert = this.certBuilderProvider.get()
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

        return this.pkiUtil.toPemEncodedString(x509cert);
    }

    private String getSCAEntitlementData(Organization org, Consumer consumer, Map<String, Content> contentMap) {
        try {
            // Convert to cert
            CertProduct certProduct = this.buildCertProduct(contentMap);
            CertEntitlement certEntitlement = this.buildCertEntitlement(consumer, certProduct);

            String json = this.mapper.writeValueAsString(certEntitlement);
            String b64compressed = this.compressAndEncodeJson(json);

            // base64 encode the payload and return it as a string
            return b64compressed;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
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

}
