package com.redhat.graviton.api.poc.resources;

import com.redhat.graviton.api.candlepin.model.*;
import com.redhat.graviton.api.candlepin.model.CPProduct.CPProductContent;
import com.redhat.graviton.api.datasource.model.ExtProduct;
import com.redhat.graviton.api.datasource.model.ExtProductChildren;
import com.redhat.graviton.impl.datasource.fs.model.FileSystemExtContent;
import com.redhat.graviton.impl.datasource.fs.model.FileSystemExtProduct;
import com.redhat.graviton.impl.datasource.fs.model.FileSystemExtProductChildren;

import com.redhat.graviton.db.curators.*;
import com.redhat.graviton.db.model.*;
import com.redhat.graviton.sync.ProductSync;
import com.redhat.graviton.sync.OrganizationSync;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.graviton.impl.datasource.fs.FileSystemDataSourceSettings;

import org.jboss.logging.Logger;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.io.*;
import java.util.*;
import java.util.stream.*;



@Path("/poc")
public class PocResource {
    private static final Logger LOG = Logger.getLogger(PocResource.class);

    @Inject
    private ObjectMapper mapper;

    @Inject
    private Provider<ProductSync> productSyncProvider;

    @Inject
    private Provider<OrganizationSync> orgSyncProvider;

    @Inject
    private ProductCurator productCurator;

    @Inject
    private OrganizationCurator orgCurator;

    @Inject
    private ConsumerCurator consumerCurator;

    @Inject
    private FileSystemDataSourceSettings settings;

    private List<CPProduct> loadProducts(File target) {
        File dir = target;
        if (!dir.canRead() || !dir.isDirectory()) {
            throw new IllegalArgumentException("target is not readable or not a directory: " + target);
        }

        List<CPProduct> products = new ArrayList<>();
        TypeReference<List<CPProduct>> typeref = new TypeReference<List<CPProduct>>() {};

        File[] jsonFiles = dir.listFiles((loc, name) -> name != null && name.endsWith(".json"));
        if (jsonFiles == null) {
            throw new IllegalStateException("jsonFiles is null...?");
        }

        for (File file : jsonFiles) {
            LOG.debugf("Reading file %s", file);

            try {
                List<CPProduct> deserialized = this.mapper.readValue(file, typeref);
                products.addAll(deserialized);
            }
            catch (Exception e) {
                throw new RuntimeException("Could not read from file " + file + ": " + e.getMessage(), e);
            }
        }

        return products;
    }

    private FileSystemExtContent convertToExtContent(CPProductContent source) {
        if (source == null) {
            return null;
        }

        CPContent content = source.getContent();
        if (content == null) {
            return null;
        }

        FileSystemExtContent extContent = new FileSystemExtContent()
            .setId(content.getId())
            .setType(content.getType())
            .setLabel(content.getLabel())
            .setName(content.getName())
            .setVendor(content.getVendor())
            .setContentUrl(content.getContentUrl())
            .setGpgUrl(content.getGpgUrl())
            .setRequiredTags(content.getRequiredTags())
            .setArches(content.getArches())
            .setReleaseVer(content.getReleaseVer())
            .setMetadataExpiration(content.getMetadataExpiration())
            .setRequiredProductIds(content.getRequiredProductIds())
            .setEnabled(source.getEnabled());

        return extContent;
    }

    private ExtProduct convertToExtProduct(CPProduct source) {
        if (source == null) {
            return null;
        }

        FileSystemExtProduct extProduct = new FileSystemExtProduct()
            .setId(source.getId())
            .setName(source.getName())
            .setMultiplier(source.getMultiplier())
            .setAttributes(source.getAttributesAsMap())
            .setDependentProductIds(source.getDependentProductIds());

        // Convert content? ARGH
        List<CPProductContent> productContent = source.getProductContent();
        if (productContent != null) {
            List<FileSystemExtContent> extContent = productContent.stream()
                .map(this::convertToExtContent)
                .collect(Collectors.toList());

            extProduct.setContent(extContent);
        }

        return extProduct;
    }

    private ExtProductChildren extractProductChildren(CPProduct product) {

        CPProduct derived = product.getDerivedProduct();
        List<CPProduct> provided = product.getProvidedProducts();

        List<String> derivedProductIds = derived != null ? List.of(derived.getId()) : null;
        List<String> providedProductIds = provided == null ? null :
            provided.stream()
                .map(CPProduct::getId)
                .collect(Collectors.toList());
        // ^ this formatting is gross. don't ever merge this into production code

        ExtProductChildren children = new FileSystemExtProductChildren()
            .setProductId(product.getId())
            .addChildrenProductIds("derived", derivedProductIds)
            .addChildrenProductIds("provided", providedProductIds);

        return children;
    }

    private void mapProducts(Map<String, CPProduct> map, Collection<CPProduct> products) {
        if (products == null) {
            return;
        }

        for (CPProduct product : products) {
            if (product == null) {
                continue;
            }

            CPProduct existing = map.get(product.getId());

            if (existing != null && !existing.equals(product)) {
                LOG.warnf("product being redefined: %s", product.getId());
                LOG.warnf("%s != %s?", existing, product);
            }

            map.put(product.getId(), product);

            this.mapProducts(map, Arrays.asList(product.getDerivedProduct()));
            this.mapProducts(map, product.getProvidedProducts());
        }
    }

    private void writeProductData(ExtProduct upstream, ExtProductChildren children) {
        File dir = settings.products().toFile();

        LOG.infof("Writing upstream product data for: %s", upstream.getId());

        File dataFile = new File(dir, String.format("%s-data.json", upstream.getId()));
        File childrenFile = new File(dir, String.format("%s-children.json", upstream.getId()));

        try {
            this.mapper.writeValue(dataFile, upstream);
            this.mapper.writeValue(childrenFile, children);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @GET
    @Path("/convert")
    @Produces(MediaType.APPLICATION_JSON)
    public void convert() {
        List<CPProduct> products = this.loadProducts(settings.raw().toFile());

        // Convert the list to a map
        Map<String, CPProduct> productMap = new HashMap<>();
        this.mapProducts(productMap, products);

        for (CPProduct product : productMap.values()) {
            ExtProduct ext = this.convertToExtProduct(product);
            ExtProductChildren children = this.extractProductChildren(product);

            this.writeProductData(ext, children);
        }
    }


    @GET
    @Path("/query/products")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Product> getProducts() {
        return this.productCurator.listProducts();
    }

    @GET
    @Path("/query/content")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Content> getContent() {
        return this.productCurator.listContent();
    }

    @GET
    @Path("/query/orgs")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Organization> getOrgs() {
        return this.orgCurator.listOrgs();
    }





    private Organization resolveOrganization(CPOwnerDTO owner) {
        if (owner == null) {
            return null;
        }

        String oid = owner.getKey();
        if (oid == null || oid.isBlank()) {
            throw new RuntimeException("No org key/oid provided");
        }

        // LOG.infof("Fetching org for OID %s", oid);
        Organization org = this.orgCurator.getOrgByOid(oid);
        if (org == null) {
            // LOG.infof("Org \"%s\" does not exist; creating new instance...", oid);
            org = new Organization()
                .setOid(oid)
                .setName(owner.getDisplayName());

            org = this.orgCurator.persist(org);
        }

        return org;
    }

    private Consumer convertToConsumer(Organization org, CPConsumerDTO dto, String username) {
        CPConsumerTypeDTO ctype = dto.getType();
        String type = ctype != null ? ctype.getLabel() : null;

        Instant now = Instant.now();

        // Sanitize facts, because apparently that's an issue...
        Map<String, String> facts = dto.getFacts();
        if (facts != null) {
            Iterator<Map.Entry<String, String>> entryIterator = facts.entrySet().iterator();
            while (entryIterator.hasNext()) {
                Map.Entry<String, String> entry = entryIterator.next();

                String key = entry.getKey();
                String value = entry.getValue();

                if (key == null || key.length() > 250) {
                    LOG.warnf("Consumer fact key is null or longer than 250 characters. Discarding fact: %s=%s", key, value);
                    entryIterator.remove();
                    continue;
                }

                if (value != null && value.length() > 250) {
                    LOG.warnf("Consumer fact value is longer than 250 characters. Discarding fact: %s=%s", key, value);
                    entryIterator.remove();
                    continue;
                }
            }
        }

        Consumer consumer = new Consumer()
            // .setOid(UUID.randomUUID().toString())
            .setOid(dto.getUuid())
            .setName(dto.getName())
            .setType(type != null ? type : "SYSTEM")
            .setOrganization(org)
            .setUsername(username)
            .setLastCheckIn(now)
            .setLastCloudProfileUpdate(now)
            .setFacts(facts);

        // v2 facts junk
        // String arch = facts.get("uname.machine");

        // consumer.setArch(arch);

        return consumer;
    }

    @Transactional
    public void updateConsumerBlock(List<CPConsumerDTO> consumerDTOs) {
        int index = -1;
        int created = 0;
        int updated = 0;

        for (CPConsumerDTO consumerDTO : consumerDTOs) {
            ++index;

            if (consumerDTO == null) {
                LOG.warnf("Consumer at index %d is null", index);
                continue;
            }

            Organization org = this.resolveOrganization(consumerDTO.getOwner());
            Consumer consumer = this.convertToConsumer(org, consumerDTO, "system");

            Consumer existing = this.consumerCurator.getConsumerByOid(consumer.getOid());
            if (existing != null) {
                ++updated;
                // LOG.infof("%d: Updating existing consumer: %s", index, consumer.getOid());
                consumer.setId(existing.getId())
                    .setCreated(existing.getCreated());

                this.consumerCurator.merge(consumer);
            }
            else {
                ++created;
                // LOG.infof("%d: Creating new consumer: %s", index, consumer.getOid());
                this.consumerCurator.persist(consumer);
            }
        }

        LOG.infof("Block complete. %d consumers created; %d updated", created, updated);
    }


    @POST
    @Path("/sync/consumers")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void syncConsumers(List<CPConsumerDTO> consumerDTOs) {
        LOG.info("Hello?");

        if (consumerDTOs == null) {
            throw new IllegalArgumentException("consumerDTOs is null");
        }

        LOG.infof("Syncing %d consumers", consumerDTOs.size());

        try {
            int count = 0;

            int cwf = 0;
            int total = 0;

            for (List<CPConsumerDTO> block : this.consumerCurator.partition(consumerDTOs, 1000)) {

                LOG.infof("Processing block %d of size: %d", ++count, block.size());
                for (CPConsumerDTO consumerDTO : block) {
                    Map<String, String> facts = consumerDTO.getFacts();

                    if (facts != null && !facts.isEmpty()) {
                        ++cwf;
                        total += facts.size();

                        LOG.infof("Consumer %s has %d facts", consumerDTO.getUuid(), facts.size());
                    }
                }


                // this.updateConsumerBlock(block);
            }

            LOG.infof("Done. Found %d consumers with facts. %d total facts", cwf, total);
        }
        catch (Exception e) {
            LOG.error("Interrupted during transaction cooldown? Okay...", e);
        }

        LOG.infof("Done syncing consumers.");
    }

    @POST
    @Path("/sync/consumerids")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> syncConsumerIds(List<CPConsumerDTO> consumerDTOs,
        @QueryParam("page") @DefaultValue("0") Integer offset,
        @QueryParam("limit") @DefaultValue("20000") Integer limit) {

        if (consumerDTOs == null) {
            throw new IllegalArgumentException("consumerDTOs is null");
        }

        return consumerDTOs.stream()
            .map(CPConsumerDTO::getId)
            .skip(offset)
            .limit(limit)
            .toList();
    }

    @Transactional
    public int processConsumerFacts(Map<String, Map<String, String>> consumerFactMap, List<String> consumerUuids) {
        int factCount = 0;

        for (String uuid : consumerUuids) {
            Map<String, String> facts = consumerFactMap.get(uuid);
            if (facts == null) {
                throw new RuntimeException("No facts found for consumer with UUID/OID: " + uuid);
            }

            Consumer consumer = this.consumerCurator.getConsumerByOid(uuid);
            if (consumer == null) {
                throw new RuntimeException("Unable to find consumer with UUID/OID: " + uuid);
            }

            LOG.infof("Updating consumer %s with %d facts", consumer.getOid(), facts.size());
            factCount += facts.size();

            consumer.setFacts(facts);
            this.consumerCurator.merge(consumer);
        }

        return factCount;
    }


    @POST
    @Path("/sync/consumerfacts")
    @Consumes(MediaType.APPLICATION_JSON)
    public void syncConsumerFacts(@QueryParam("file") String filename) {
        Map<String, Map<String, String>> consumerFactMap = new HashMap<>();
        int factCount = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader("/home/crog/devel/graviton/" + filename))) {
            reader.lines()
                .map(line -> line.split("\t"))
                .forEach(chunks -> consumerFactMap.computeIfAbsent(chunks[0], key -> new HashMap<>()).put(chunks[2], chunks[1]));

            for (List<String> uuidBlock : this.consumerCurator.partition(consumerFactMap.keySet(), 1000)) {
                factCount += this.processConsumerFacts(consumerFactMap, uuidBlock);
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

        LOG.infof("Done. %d consumers updated with %d total facts", consumerFactMap.size(), factCount);
    }

    @GET
    @Path("consumers/anyfacts")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Consumer> listConsumersByAnyFacts(@QueryParam("keys") List<String> keys) {
        return this.consumerCurator.findConsumersWithAnyFacts(keys);
    }

    @GET
    @Path("consumers/allfacts")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Consumer> listConsumersByAllFacts(@QueryParam("keys") List<String> keys) {
        return this.consumerCurator.findConsumersWithAllFacts(keys);
    }

    @GET
    @Path("consumers/factpairs")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Consumer> findConsumersWithFacts(@QueryParam("facts") List<String> kvpairs) {
        Map<String, String> factmap = kvpairs.stream()
            .map(pair -> pair.split(",", 2))
            .collect(Collectors.toMap(pair -> pair[0], pair -> pair[1]));

        LOG.infof("Fetching consumers by fact map: %s", factmap);

        return this.consumerCurator.findConsumersWithFacts(factmap);
    }


    @POST
    @Path("/sync/products")
    @Produces(MediaType.APPLICATION_JSON)
    public void syncProducts(
        @QueryParam("oid") List<String> productOids) {

        LOG.info("INVOKING PRODUCT SYNC");
        this.productSyncProvider.get()
            .addProductOids(productOids)
            .execute();
        LOG.info("DONE!");
    }

    @POST
    @Path("/sync/orgs")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void syncOrgs(List<String> orgOids) {

        LOG.debugf("RECEIVED ORG LIST: %s", orgOids);

        if (orgOids == null || orgOids.isEmpty()) {
            LOG.info("FETCHING ORGANIZATION LIST");
            File baseDir = settings.subscriptions().toFile();

            orgOids = Stream.of(baseDir.list())
                .map(filename -> filename.substring(0, filename.length() - 4))
                .collect(Collectors.toList());
        }

        LOG.infof("SYNCING %d ORGS...", orgOids.size());

        int count = 0;
        for (String orgOid : orgOids) {
            try {
                this.orgSyncProvider.get()
                    .setOrganizationOid(orgOid)
                    .setCreateOrg(true)
                    .execute();
            }
            catch (Exception e) {
                LOG.errorf("EXCEPTION OCCURRED WHILE REFRESHING ORG %s", orgOid, e);
            }

            count += 1;
            if (count % 10000 == 0) {
                LOG.infof("COMPLETED %d ORG REFRESHES", count);
            }
        }

        LOG.infof("DONE. %d ORGS SYNC'D", count);
    }

    @GET
    @Path("/sync/orgs/{org_oid}")
    @Produces(MediaType.APPLICATION_JSON)
    public void syncOrg(
        @PathParam("org_oid") String orgOid,
        @QueryParam("sub_oid") List<String> subscriptionOids,
        @QueryParam("create_org") Boolean createOrg) {

        LOG.info("INVOKING ORGANIZATION SYNC");
        this.orgSyncProvider.get()
            .setOrganizationOid(orgOid)
            .addSubscriptionOids(subscriptionOids)
            .setCreateOrg(createOrg != null && createOrg)
            .execute();
        LOG.info("DONE!");
    }

    // fun queries for fun

    @GET
    @Path("/query/products/orgs")
    @Produces(MediaType.APPLICATION_JSON)
    public Set<String> queryOrgsUsingProducts(
        @QueryParam("oid") List<String> prodOids) {

        return this.productCurator.getOrgsUsingProducts(prodOids);
    }

    @GET
    @Path("/query/orgs/{org_oid}/content")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Content> getOrgContent(
        @PathParam("org_oid") String orgOid,
        @QueryParam("active") Boolean activeOnly) {

        return this.productCurator.getOrgContent(orgOid, activeOnly != null && activeOnly)
            .values();
    }


    @GET
    @Path("/query/orgs/{org_oid}/content/{content_oid}")
    @Produces(MediaType.APPLICATION_JSON)
    public boolean canOrgAccessContent(
        @PathParam("org_oid") String orgOid,
        @PathParam("content_oid") String contentOid) {

        return this.productCurator.canOrgAccessContent(orgOid, contentOid);
    }

    @GET
    @Path("/query/orgs/{org_oid}/products/{product_oid}")
    @Produces(MediaType.APPLICATION_JSON)
    public boolean canOrgAccessProduct(
        @PathParam("org_oid") String orgOid,
        @PathParam("product_oid") String productOid) {

        return this.productCurator.canOrgAccessProduct(orgOid, productOid);
    }
}
