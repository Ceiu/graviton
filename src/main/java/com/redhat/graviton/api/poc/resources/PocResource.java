package com.redhat.graviton.api.poc.resources;

import com.redhat.graviton.api.candlepin.model.CPContent;
import com.redhat.graviton.api.candlepin.model.CPProduct;
import com.redhat.graviton.api.candlepin.model.CPProduct.CPProductContent;
import com.redhat.graviton.api.datasource.model.ExtContent;
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

import org.jboss.logging.Logger;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.POST;
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

    @Inject
    private ProductCurator productCurator;


    private List<CPProduct> loadProducts(String target) {
        File dir = new File(target);
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
        File dir = new File("/home/crog/devel/subscription_data/product_data");

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
        List<CPProduct> products = this.loadProducts("/home/crog/devel/subscription_data/raw");

        // Convert the list to a map
        Map<String, CPProduct> productMap = new HashMap<>();
        this.mapProducts(productMap, products);

        for (CPProduct product : productMap.values()) {
            ExtProduct ext = this.convertToExtProduct(product);
            ExtProductChildren children = this.extractProductChildren(product);

            this.writeProductData(ext, children);
        }
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
            File baseDir = new File("/home/crog/devel/subscription_data/subscriptions");

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
    @Path("/query/orgs/{org_oid}/content/count")
    @Produces(MediaType.APPLICATION_JSON)
    public int getOrgContentCount(
        @PathParam("org_oid") String orgOid,
        @QueryParam("active") Boolean activeOnly) {

        return this.productCurator.getOrgContent(orgOid, activeOnly != null && activeOnly).size();
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
