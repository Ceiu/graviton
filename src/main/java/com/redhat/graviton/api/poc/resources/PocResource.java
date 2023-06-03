package com.redhat.graviton.api.poc.resources;

import com.redhat.graviton.api.poc.model.*;
import com.redhat.graviton.api.poc.model.ExtProduct.ExtProductContent;
import com.redhat.graviton.api.datasource.model.*;
import com.redhat.graviton.sync.ProductSync;
import com.redhat.graviton.sync.OrganizationSync;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.jboss.logging.Logger;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

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


    private List<ExtProduct> loadProducts(String target) {
        File dir = new File(target);
        if (!dir.canRead() || !dir.isDirectory()) {
            throw new IllegalArgumentException("target is not readable or not a directory: " + target);
        }

        List<ExtProduct> products = new ArrayList<>();
        TypeReference<List<ExtProduct>> typeref = new TypeReference<List<ExtProduct>>() {};

        File[] jsonFiles = dir.listFiles((loc, name) -> name != null && name.endsWith(".json"));
        if (jsonFiles == null) {
            throw new IllegalStateException("jsonFiles is null...?");
        }

        for (File file : jsonFiles) {
            LOG.debugf("Reading file %s", file);

            try {
                List<ExtProduct> deserialized = this.mapper.readValue(file, typeref);
                products.addAll(deserialized);
            }
            catch (Exception e) {
                throw new RuntimeException("Could not read from file " + file + ": " + e.getMessage(), e);
            }
        }

        return products;
    }

    private UpstreamContent convertToUpstreamContent(ExtProductContent source) {
        if (source == null) {
            return null;
        }

        ExtContent content = source.getContent();
        if (content == null) {
            return null;
        }

        UpstreamContent upstreamContent = new UpstreamContent()
            .setCreated(content.getCreated())
            .setUpdated(content.getUpdated())
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

        return upstreamContent;
    }

    private UpstreamProduct convertToUpstreamProduct(ExtProduct source) {
        if (source == null) {
            return null;
        }

        UpstreamProduct upstreamProduct = new UpstreamProduct()
            .setCreated(source.getCreated())
            .setUpdated(source.getUpdated())
            .setId(source.getId())
            .setName(source.getName())
            .setMultiplier(source.getMultiplier())
            .setAttributes(source.getAttributesAsMap())
            .setDependentProductIds(source.getDependentProductIds());

        // Convert content? ARGH
        List<ExtProductContent> productContent = source.getProductContent();
        if (productContent != null) {
            List<UpstreamContent> upstreamContent = productContent.stream()
                .map(this::convertToUpstreamContent)
                .collect(Collectors.toList());

            upstreamProduct.setContent(upstreamContent);
        }
        else {
            upstreamProduct.setContent(null);
        }

        return upstreamProduct;
    }

    private UpstreamProductTree convertToProductTree(ExtProduct product) {

        ExtProduct derived = product.getDerivedProduct();
        List<String> derivedProductOids = derived != null ? List.of(derived.getId()) : null;

        List<ExtProduct> provided = product.getProvidedProducts();
        List<String> providedProductOids = provided == null ? null :
            provided.stream()
                .map(ExtProduct::getId)
                .collect(Collectors.toList());
        // ^ this formatting is gross. don't ever merge this into production code

        UpstreamProductTree tree = new UpstreamProductTree()
            .setOid(product.getId())
            .setDerivedOids(derivedProductOids)
            .setProvidedOids(providedProductOids);

        return tree;
    }

    private void mapProducts(Map<String, ExtProduct> map, Collection<ExtProduct> products) {
        if (products == null) {
            return;
        }

        for (ExtProduct product : products) {
            if (product == null) {
                continue;
            }

            ExtProduct existing = map.get(product.getId());

            if (existing != null && !existing.equals(product)) {
                LOG.warnf("product being redefined: %s", product.getId());
                LOG.warnf("%s != %s?", existing, product);
            }

            map.put(product.getId(), product);

            this.mapProducts(map, Arrays.asList(product.getDerivedProduct()));
            this.mapProducts(map, product.getProvidedProducts());
        }
    }

    private void writeProductData(UpstreamProduct upstream, UpstreamProductTree tree) {
        File dir = new File("/home/crog/devel/subscription_data/product_data");

        LOG.infof("Writing upstream product data for: %s", upstream.getId());

        File dataFile = new File(dir, String.format("%s-data.json", upstream.getId()));
        File treeFile = new File(dir, String.format("%s-tree.json", upstream.getId()));

        try {
            this.mapper.writeValue(dataFile, upstream);
            this.mapper.writeValue(treeFile, tree);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @GET
    @Path("/convert")
    @Produces(MediaType.APPLICATION_JSON)
    public String convert() {
        List<ExtProduct> products = this.loadProducts("/home/crog/devel/subscription_data/raw");

        // Convert the list to a map
        Map<String, ExtProduct> productMap = new HashMap<>();
        this.mapProducts(productMap, products);

        for (ExtProduct product : productMap.values()) {
            UpstreamProduct upstream = this.convertToUpstreamProduct(product);
            UpstreamProductTree tree = this.convertToProductTree(product);

            this.writeProductData(upstream, tree);
        }

        return "convert";
    }

    @GET
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

    @GET
    @Path("/sync/owners")
    @Produces(MediaType.APPLICATION_JSON)
    public void syncOrgs() {

        LOG.info("FETCHING ORGANIZATION LIST");
        File baseDir = new File("/home/crog/devel/subscription_data/subscriptions");

        Set<String> orgOids = Stream.of(baseDir.list())
            .map(filename -> filename.substring(0, filename.length() - 4))
            .collect(Collectors.toSet());

        LOG.infof("SYNCING %d ORGS", orgOids.size());

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
    }

    @GET
    @Path("/sync/owners/{org_oid}")
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
}
