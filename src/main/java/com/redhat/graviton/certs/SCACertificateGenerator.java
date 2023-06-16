package com.redhat.graviton.certs;

import com.redhat.graviton.api.candlepin.cert.*;
import com.redhat.graviton.db.model.*;
import com.redhat.graviton.db.curators.ProductCurator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.annotation.JsonInclude;
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
import java.io.ByteArrayInputStream;
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
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;



@Dependent
public class SCACertificateGenerator {
    private static final Logger LOG = Logger.getLogger(SCACertificateGenerator.class);

    private final CertificateAuthority certAuthority;
    private final KeyGenerator keyGenerator;
    private final Provider<X509CertificateBuilder> certBuilderProvider;
    private final PKIUtility pkiUtil;

    private final ProductCurator productCurator;
    private final ObjectMapper mapper;




    @Inject
    public SCACertificateGenerator(CertificateAuthority certAuthority, KeyGenerator keyGenerator,
        Provider<X509CertificateBuilder> certBuilderProvider, PKIUtility pkiUtil,
        ProductCurator productCurator) {

        this.certAuthority = Objects.requireNonNull(certAuthority);
        this.keyGenerator = Objects.requireNonNull(keyGenerator);
        this.certBuilderProvider = Objects.requireNonNull(certBuilderProvider);
        this.pkiUtil = Objects.requireNonNull(pkiUtil);
        this.productCurator = Objects.requireNonNull(productCurator);

        this.mapper = new ObjectMapper();
        this.mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
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

        Map<String, Content> contentMap = this.productCurator.getOrgContent(org.getOid(), true);
        CertProduct certProduct = this.buildCertProduct(org, contentMap);
        List<CertContent> contentList = certProduct.getContent();

        byte[] contentExtension = this.buildContentExtension(contentList);


        return this.certBuilderProvider.get()
            .generateCertificateSerial()
            .setDistinguishedName(distinguishedName)
            .setValidAfter(validAfter)
            .setValidUntil(validUntil)
            .setKeyPair(keypair)

            // Magic Red Hat extension OID based on CP code (1.3.6.1.4.1.2312.9. = Red Hat OID prefix)
            .addExtension(new X509StringExtension("1.3.6.1.4.1.2312.9.6", false, "3.4"))
            .addExtension(new X509ByteExtension("1.3.6.1.4.1.2312.9.7", false, contentExtension))
            .addExtension(new X509StringExtension("1.3.6.1.4.1.2312.9.8", false, "OrgLevel"))

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
            CertProduct certProduct = this.buildCertProduct(org, contentMap);
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

    private CertProduct buildCertProduct(Organization org, Map<String, Content> contentMap) {

        // When it comes to filtering content, there are a lot of bits CP does that are
        // implicitly ignored by way of the product pass into the X509ExtensionUtil being a
        // minimialist product. This is probably not good (tm) long term, but makes life easier
        // for me here.
        List<CertContent> certContent = contentMap.values()
            .stream()
            .map(content -> this.convertToCertContent(org, content))
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

    private CertContent convertToCertContent(Organization org, Content content) {
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
            .setEnabled(content.isEnabled())

            .setPath(this.getContentPath(org, content.getContentUrl()));

        // Include required tags as a list if present...
        String requiredTags = content.getRequiredTags();
        if (requiredTags != null) {
            List<String> parsedTags = List.of(requiredTags.trim().split("\\s*,[\\s,]*"));
            certContent.setRequiredTags(parsedTags);
        }

        return certContent;
    }

    private String getContentPath(Organization org, String contentUrl) {
        if (contentUrl == null || contentUrl.matches("^\\w+://")) {
            return contentUrl;
        }

        StringBuilder path = new StringBuilder("/")
            .append(org.getOid());

        if (!contentUrl.startsWith("/")) {
            path.append("/");
        }

        path.append(contentUrl);

        return path.toString();
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
            .setEnd(end.toString())
            .setNumber("")
            .setAccount("")
            .setContract("");

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



















    private static final Object END_NODE = new Object();
    private int huffNodeId = 0;
    private int pathNodeId = 0;


    private byte[] buildContentExtension(List<CertContent> contentList) {
        try {
            PathNode treeRoot = makePathTree(contentList, new PathNode());
            List<String> nodeStrings = orderStrings(treeRoot);
            if (nodeStrings.size() == 0) {
                return new byte[0];
            }

            ByteArrayOutputStream data = new ByteArrayOutputStream();

            byte[] bytes = byteProcess(nodeStrings);
            data.write(bytes);
            List<HuffNode> stringHuffNodes = this.getStringNodeList(nodeStrings);
            HuffNode stringTrieParent = this.makeTrie(stringHuffNodes);
            List<PathNode> orderedNodes = this.orderNodes(treeRoot);
            List<HuffNode> pathNodeHuffNodes = this.getPathNodeNodeList(orderedNodes);
            HuffNode pathNodeTrieParent = this.makeTrie(pathNodeHuffNodes);
            byte[] dictionary = this.makeNodeDictionary(stringTrieParent, pathNodeTrieParent, orderedNodes);
            data.write(dictionary);

            return data.toByteArray();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<HuffNode> getStringNodeList(List<String> pathStrings) {
        List<HuffNode> nodes = new ArrayList<>();
        int idx = 1;
        for (String part : pathStrings) {
            nodes.add(new HuffNode(part, idx++));
        }
        nodes.add(new HuffNode(END_NODE, idx));
        return nodes;
    }

    private List<HuffNode> getPathNodeNodeList(List<PathNode> pathNodes) {
        List<HuffNode> nodes = new ArrayList<>();
        int idx = 0;
        for (PathNode pn : pathNodes) {
            nodes.add(new HuffNode(pn, idx++));
        }
        return nodes;
    }

    private HuffNode makeTrie(List<HuffNode> nodesList) {
        // drop the first node if path node value, it is not needed
        if (nodesList.get(0).getValue() instanceof PathNode) {
            nodesList.remove(0);
        }

        while (nodesList.size() > 1) {
            int node1 = findSmallest(-1, nodesList);
            int node2 = findSmallest(node1, nodesList);
            HuffNode hn1 = nodesList.get(node1);
            HuffNode hn2 = nodesList.get(node2);
            HuffNode merged = mergeNodes(hn1, hn2);
            nodesList.remove(hn1);
            nodesList.remove(hn2);
            nodesList.add(merged);
        }

        return nodesList.get(0);
    }

    private int findSmallest(int exclude, List<HuffNode> nodes) {
        int smallest = -1;
        for (int index = 0; index < nodes.size(); index++) {
            if (index == exclude) {
                continue;
            }
            if (smallest == -1 || nodes.get(index).getWeight() <
                nodes.get(smallest).getWeight()) {
                smallest = index;
            }
        }
        return smallest;
    }

    private HuffNode mergeNodes(HuffNode left, HuffNode right) {
        return new HuffNode(null, left.weight + right.weight, left, right);
    }


    private byte[] makeNodeDictionary(HuffNode stringParent,
        HuffNode pathNodeParent, List<PathNode> pathNodes) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int nodeSize = pathNodes.size();
        if (nodeSize > 127) {
            ByteArrayOutputStream countBaos = new ByteArrayOutputStream();
            boolean start = false;
            for (byte b : toByteArray(nodeSize)) {
                if (b == 0 && !start) {
                    continue;
                }
                else {
                    countBaos.write(b);
                    start = true;
                }
            }
            baos.write(128 + countBaos.size());
            countBaos.close();
            baos.write(countBaos.toByteArray());
        }
        else {
            baos.write(nodeSize);
        }
        StringBuilder bits = new StringBuilder();
        String endNodeLocation = findHuffPath(stringParent, END_NODE);
        for (PathNode pn : pathNodes) {
            for (NodePair np : pn.getChildren()) {
                bits.append(findHuffPath(stringParent, np.getName()));
                bits.append(findHuffPath(pathNodeParent, np.getConnection()));
            }
            bits.append(endNodeLocation);
            while (bits.length() >= 8) {
                int next = 0;
                for (int i = 0; i < 8; i++) {
                    next = (byte) next << 1;
                    if (bits.charAt(i) == '1') {
                        next++;
                    }
                }
                baos.write(next);
                bits.delete(0, 8);
            }
        }

        if (bits.length() > 0) {
            int next = 0;
            for (int i = 0;  i < 8; i++) {
                next = (byte) next << 1;
                if (i < bits.length() && bits.charAt(i) == '1') {
                    next++;
                }
            }
            baos.write(next);
        }
        byte[] result = baos.toByteArray();

        baos.close();
        return result;
    }

    private byte[] toByteArray(int value) {
        return new byte[] {
            (byte) (value >> 24),
            (byte) (value >> 16),
            (byte) (value >> 8),
            (byte) value};
    }

    private String findHuffPath(HuffNode trie, Object need) {
        HuffNode left = trie.getLeft();
        HuffNode right = trie.getRight();
        if (left != null && left.getValue() != null) {
            if (need.equals(left.getValue())) {
                return "0";
            }
        }
        if (right != null && right.getValue() != null) {
            if (need.equals(right.getValue())) {
                return "1";
            }
        }
        if (left != null) {
            String leftPath = findHuffPath(left, need);
            if (leftPath.length() > 0) {
                return "0" + leftPath;
            }
        }
        if (right != null) {
            String rightPath = findHuffPath(right, need);
            if (rightPath.length() > 0) {
                return "1" + rightPath;
            }
        }
        return "";
    }

    private PathNode makePathTree(List<CertContent> contents, PathNode parent) {
        PathNode endMarker = new PathNode();
        for (CertContent c : contents) {
            String path = c.getPath();

            StringTokenizer st = new StringTokenizer(path, "/");
            makePathForURL(st, parent, endMarker);
        }

        condenseSubTreeNodes(endMarker);

        return parent;
    }

    private List<String> orderStrings(PathNode parent) throws IOException {
        List<String> parts = new ArrayList<>();
        // walk tree to make string map
        Map<String, Integer> segments = new HashMap<>();
        Set<PathNode> nodes = new HashSet<>();
        buildSegments(segments, nodes, parent);
        for (Map.Entry<String, Integer> entry : segments.entrySet()) {
            String part = entry.getKey();
            if (!part.equals("")) {
                int count = entry.getValue();
                if (parts.size() == 0) {
                    parts.add(part);
                }
                else {
                    int pos = parts.size();
                    for (int i = 0; i < parts.size(); i++) {
                        if (count < segments.get(parts.get(i))) {
                            pos = i;
                            break;
                        }
                    }
                    parts.add(pos, part);
                }
            }
        }

        return parts;
    }

    private List<PathNode> orderNodes(PathNode treeRoot) {
        List<PathNode> result = new ArrayList<>();

        // walk tree to make string map
        Set<PathNode> nodes =  getPathNodes(treeRoot);
        for (PathNode pn : nodes) {
            int count = pn.getParents().size();
            if (nodes.size() == 0) {
                nodes.add(pn);
            }
            else {
                int pos = result.size();
                for (int i = 0; i < result.size(); i++) {
                    if (count <= result.get(i).getParents().size()) {
                        pos = i;
                        break;
                    }
                    if (count == result.get(i).getParents().size()) {
                        if (pn.getId() < result.get(i).getId()) {
                            pos = i;
                        }
                        else {
                            pos = i + 1;
                        }
                        break;
                    }
                }
                result.add(pos, pn);
            }
        }
        // single node plus term node. We need to have one more for huffman trie
        if (result.size() == 2) {
            result.add(new PathNode());
        }

        return result;
    }

    private byte[] byteProcess(List<String> entries) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DeflaterOutputStream dos = new DeflaterOutputStream(baos,
            new Deflater(Deflater.BEST_COMPRESSION));
        for (String segment : entries) {
            dos.write(segment.getBytes(StandardCharsets.UTF_8));
            dos.write("\0".getBytes(StandardCharsets.UTF_8));
        }
        dos.finish();
        dos.close();
        return baos.toByteArray();
    }

    private void condenseSubTreeNodes(PathNode location) {
        // "equivalent" parents are merged
        List<PathNode> parentResult = new ArrayList<>(location.getParents());
        for (PathNode parent1 : location.getParents()) {
            if (!parentResult.contains(parent1)) {
                continue;
            }
            for (PathNode parent2 : location.getParents()) {
                if (!parentResult.contains(parent2) ||
                    parent2.getId() == parent1.getId()) {
                    continue;
                }
                if (parent1.isEquivalentTo(parent2)) {
                    // we merge them into smaller Id
                    PathNode merged = parent1.getId() < parent2.getId() ?
                        parent1 : parent2;
                    PathNode toRemove = parent1.getId() < parent2.getId() ?
                        parent2 : parent1;

                    // track down the name of the string in the grandparent
                    //  that points to parent
                    String name = "";
                    PathNode oneParent = toRemove.getParents().get(0);
                    for (NodePair child : oneParent.getChildren()) {
                        if (child.getConnection().getId() == toRemove.getId()) {
                            name = child.getName();
                            break;
                        }
                    }

                    // copy grandparents to merged parent node.
                    List<PathNode> movingParents = toRemove.getParents();
                    merged.addParents(movingParents);

                    // all grandparents with name now point to merged node
                    for (PathNode pn : toRemove.getParents()) {
                        for (NodePair child : pn.getChildren()) {
                            if (child.getName().equals(name) &&
                                child.getConnection().isEquivalentTo(merged)) {
                                child.setConnection(merged);
                            }
                        }
                    }
                    parentResult.remove(toRemove);
                }
            }
        }
        location.setParents(parentResult);
        for (PathNode pn : location.getParents()) {
            condenseSubTreeNodes(pn);
        }
    }

    private Set<PathNode> getPathNodes(PathNode treeRoot) {
        Set<PathNode> nodes = new HashSet<>();
        nodes.add(treeRoot);
        for (NodePair np : treeRoot.getChildren()) {
            nodes.addAll(getPathNodes(np.getConnection()));
        }
        return nodes;
    }

    private void buildSegments(Map<String, Integer> segments,
        Set<PathNode> nodes, PathNode parent) {
        if (!nodes.contains(parent)) {
            nodes.add(parent);
            for (NodePair np : parent.getChildren()) {
                Integer count = segments.get(np.getName());
                if (count == null) {
                    count = 0;
                }
                segments.put(np.getName(), ++count);
                buildSegments(segments, nodes, np.getConnection());
            }
        }
    }


    private void makePathForURL(StringTokenizer st, PathNode parent, PathNode endMarker) {
        if (st.hasMoreTokens()) {
            String childVal = st.nextToken();
            if (childVal.equals("")) {
                return;
            }
            boolean isNew = true;
            for (NodePair child : parent.getChildren()) {
                if (child.getName().equals(childVal) &&
                    !child.getConnection().equals(endMarker)) {
                    makePathForURL(st, child.getConnection(), endMarker);
                    isNew = false;
                }
            }

            if (isNew) {
                PathNode next;
                if (st.hasMoreTokens()) {
                    next = new PathNode();
                    parent.addChild(new NodePair(childVal, next));
                    next.addParent(parent);
                    makePathForURL(st, next, endMarker);
                }
                else {
                    parent.addChild(new NodePair(childVal, endMarker));
                    if (!endMarker.getParents().contains(parent)) {
                        endMarker.addParent(parent);
                    }
                }
            }
        }
    }


    public class HuffNode {
        private long id = 0;
        private Object value = null;
        private int weight = 0;
        private HuffNode left = null;
        private HuffNode right = null;

        public HuffNode(Object value, int weight, HuffNode left, HuffNode right) {
            this.value = value;
            this.weight = weight;
            this.left = left;
            this.right = right;
            this.id = huffNodeId++;
        }
        public HuffNode(Object value, int weight) {
            this.value = value;
            this.weight = weight;
            this.id = huffNodeId++;
        }

        public Object getValue() {
            return this.value;
        }

        public int getWeight() {
            return this.weight;
        }

        public HuffNode getLeft() {
            return this.left;
        }

        public HuffNode getRight() {
            return this.right;
        }

        long getId() {
            return this.id;
        }

        public String toString() {
            return String.format("HuffNode: [id: %s, weight: %d, value: %s -- left: %s, right: %s]",
                this.id, this.weight, this.value, this.left, this.right);
        }
    }

    public class PathNode {
        private long id = 0;
        private List<NodePair> children = new ArrayList<>();
        private List<PathNode> parents = new ArrayList<>();

        public PathNode() {
            this.id = pathNodeId++;
        }

        public long getId() {
            return id;
        }

        void addChild(NodePair cp) {
            this.children.add(cp);
        }

        void addParent(PathNode cp) {
            if (!parents.contains(cp)) {
                this.parents.add(cp);
            }
        }

        public List<NodePair> getChildren() {
            Collections.sort(this.children);
            return this.children;
        }

        List<PathNode> getParents() {
            return this.parents;
        }

        void setParents(List<PathNode> parents) {
            this.parents = parents;
        }

        void addParents(List<PathNode> parents) {
            for (PathNode pn : parents) {
                addParent(pn);
            }
        }

        boolean isEquivalentTo(PathNode that) {
            if (this.getId() == that.getId()) {
                return true;
            }
            // same number of children with the same names for child nodes
            if (this.getChildren().size() != that.getChildren().size()) {
                return false;
            }
            for (NodePair thisnp : this.getChildren()) {
                boolean found = false;
                for (NodePair thatnp : that.getChildren()) {
                    if (thisnp.getName().equals(thatnp.getName())) {
                        if (thisnp.getConnection().isEquivalentTo(thatnp.getConnection())) {
                            found = true;
                            break;
                        }
                        else {
                            return false;
                        }
                    }
                }
                if (!found) {
                    return false;
                }
            }
            return true;
        }

        public String toString() {
            StringBuilder parentList = new StringBuilder("ID: ");
            parentList.append(id).append(", Parents");
            for (PathNode parent : parents) {
                parentList.append(": ").append(parent.getId());
            }

            // "ID: " + id + ", Parents" + parentList + ", Children: " + children;
            return parentList.append(", Children: ").append(children).toString();
        }
    }

    public static class NodePair implements Comparable{
        private String name;
        private PathNode connection;

        NodePair(String name, PathNode connection) {
            this.name = name;
            this.connection = connection;
        }

        public String getName() {
            return name;
        }

        public PathNode getConnection() {
            return connection;
        }

        void setConnection(PathNode connection) {
            this.connection = connection;
        }

        public String toString() {
            return "Name: " + name + ", Connection: " + connection.getId();
        }

        /* (non-Javadoc)
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        @Override
        public int compareTo(Object other) {
            return this.name.compareTo(((NodePair) other).name);
        }

        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }

            if (!(other instanceof NodePair)) {
                return false;
            }

            return this.name.equals(((NodePair) other).getName());
        }

        public int hashCode() {
            return name.hashCode();
        }
    }

}
