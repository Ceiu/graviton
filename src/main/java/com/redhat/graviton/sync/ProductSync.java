package com.redhat.graviton.sync;

import com.redhat.graviton.api.datasource.ProductDataSource;
import com.redhat.graviton.db.model.*;
import com.redhat.graviton.db.curators.*;
import com.redhat.graviton.api.datasource.model.*;
import com.redhat.graviton.sync.graph.*;

import java.util.Set;
import java.util.HashSet;

import org.jboss.logging.Logger;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.enterprise.context.Dependent;
import jakarta.transaction.Transactional;

import java.io.File;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;


@Dependent
public class ProductSync {
    private static final Logger LOG = Logger.getLogger(ProductSync.class);

    private final ProductDataSource productDataSource;
    private final Provider<PersistenceVisitor> visitorProvider;
    private final ProductCurator productCurator;

    private final Set<String> productOids;

    @Inject
    public ProductSync(ProductDataSource productDataSource, Provider<PersistenceVisitor> visitorProvider,
        ProductCurator productCurator) {

        this.productDataSource = Objects.requireNonNull(productDataSource);
        this.visitorProvider = Objects.requireNonNull(visitorProvider);
        this.productCurator = Objects.requireNonNull(productCurator);

        this.productOids = new HashSet<>();
    }

    public ProductSync addProductOids(Collection<String> oids) {
        if (oids != null) {
            oids.stream()
                .filter(oid -> oid != null && !oid.isEmpty())
                .forEach(this.productOids::add);
        }

        return this;
    }

    public ProductSync addProductOids(String... oids) {
        return this.addProductOids(Arrays.asList(oids));
    }

    public ProductSync addProductOid(String oid) {
        return this.addProductOids(oid);
    }


    private void populateLocalProductNodes(Map<String, ProductNode> productMap, Collection<Product> products) {
        for (Product product : products) {
            String oid = product.getOid();

            ProductNode node = productMap.computeIfAbsent(oid, (key) -> new ProductNode())
                .setLocalEntity(product);
        }
    }

    private void populateLocalProductLinks(Map<String, ProductNode> productMap, Collection<ProductOidGraphNode> links) {
        for (ProductOidGraphNode link : links) {
            if (link.depth() != 1) {
                continue;
            }

            ProductNode pnode = productMap.get(link.productOid());
            ProductNode cnode = productMap.get(link.childOid());
            if (pnode == null || cnode == null) {
                throw new IllegalStateException("pnode or cnode == null: " + pnode + ", " + cnode + ", " + link);
            }

            ProductLink plink = pnode.getProductLink(link.childOid());
            if (plink == null) {
                plink = new ProductLink(pnode, cnode, link.type());
                pnode.addProductLink(link.childOid(), plink);
                cnode.addParent(plink); // probably unnecessary
            }

            plink.setLocalLinkPresence(true);
        }
    }

    private void populateLocalContentNodes(Map<String, ContentNode> contentMap, Collection<Content> contents) {
        for (Content content : contents) {
            String oid = content.getOid();

            ContentNode node = contentMap.computeIfAbsent(oid, (key) -> new ContentNode())
                .setLocalEntity(content);
        }
    }

    private void populateLocalContentLinks(Map<String, ProductNode> productMap, Map<String, ContentNode> contentMap,
        Collection<ProductContentOidLink> pclinks) {

        for (ProductContentOidLink pclink : pclinks) {
            ProductNode pnode = productMap.get(pclink.productOid());
            ContentNode cnode = contentMap.get(pclink.contentOid());
            if (pnode == null || cnode == null) {
                throw new IllegalStateException("pnode or cnode == null: " + pnode + ", " + cnode + ", " + pclink);
            }

            ContentLink clink = pnode.getContentLink(pclink.contentOid());
            if (clink == null) {
                clink = new ContentLink(pnode, cnode);
                pnode.addContentLink(pclink.contentOid(), clink);
                cnode.addParent(clink); // probably unnecessary
            }

            clink.setLocalLinkPresence(true);
        }
    }

    private void populateExtProductNodes(Map<String, ProductNode> productMap, Map<String, ContentNode> contentMap,
        Collection<UpstreamProduct> products) {

        for (UpstreamProduct product : products) {
            String oid = product.getId();

            ProductNode node = productMap.computeIfAbsent(oid, (key) -> new ProductNode())
                .setExternalEntity(product);

            List<UpstreamContent> contents = product.getContent();
            this.populateExtContentNodes(node, contentMap, contents);
        }
    }

    private void populateExtProductTrees(Map<String, ProductNode> productMap, Collection<UpstreamProductTree> productTrees) {
        for (UpstreamProductTree productTree : productTrees) {
            String oid = productTree.getOid();

            ProductNode node = productMap.get(oid);
            if (node == null) {
                throw new IllegalStateException("product tree received for a product that isn't mapped...? oid: " + oid);
            }

            List<String> derivedProductOids = productTree.getDerivedOids();
            if (derivedProductOids != null) {
                for (String dpOid : derivedProductOids) {
                    this.populateExtProductLink(node, dpOid, "derived", productMap);
                }
            }

            List<String> providedProductOids = productTree.getProvidedOids();
            if (providedProductOids != null) {
                for (String ppOid : providedProductOids) {
                    this.populateExtProductLink(node, ppOid, "provided", productMap);
                }
            }
        }
    }

    private void populateExtProductLink(ProductNode pnode, String childOid, String type, Map<String, ProductNode> productMap) {
        ProductNode cnode = productMap.get(childOid);
        if (cnode == null) {
            LOG.errorf("product tree references child that doesn't exist? %s => %s??", pnode, childOid);
            throw new IllegalStateException("product tree references a child that doesn't exist?");
        }

        ProductLink plink = pnode.getProductLink(childOid);
        if (plink == null) {
            plink = new ProductLink(pnode, cnode, type);
            pnode.addProductLink(childOid, plink);
            cnode.addParent(plink);
        }

        plink.setExternalLinkPresence(true);
    }

    private void populateExtContentNodes(ProductNode pnode, Map<String, ContentNode> contentMap, Collection<UpstreamContent> contents) {
        for (UpstreamContent content : contents) {
            String oid = content.getId();

            ContentNode cnode = contentMap.computeIfAbsent(oid, (key) -> new ContentNode())
                .setExternalEntity(content);

            ContentLink clink = pnode.getContentLink(oid);
            if (clink == null) {
                clink = new ContentLink(pnode, cnode);
                pnode.addContentLink(oid, clink);
                cnode.addParent(clink);
            }

            clink.setExternalLinkPresence(true);
        }
    }

    /**
     *
     *
     */
    private Map<String, UpstreamProductTree> resolveUpstreamProductTrees(Set<String> oids) {
        Map<String, UpstreamProductTree> trees = new HashMap<>();

        do {
            Map<String, UpstreamProductTree> treeblock = this.productDataSource.getProductTrees(oids);
            Set<String> childrenOids = new HashSet<>();

            for (Map.Entry<String, UpstreamProductTree> entry : treeblock.entrySet()) {
                UpstreamProductTree tree = entry.getValue();
                trees.put(entry.getKey(), tree);

                if (tree.getDerivedOids() != null) {
                    childrenOids.addAll(tree.getDerivedOids());
                }

                if (tree.getProvidedOids() != null) {
                    childrenOids.addAll(tree.getProvidedOids());
                }
            }

            childrenOids.removeAll(treeblock.keySet());

            if (childrenOids.isEmpty()) {
                break;
            }

            oids = childrenOids;
        }
        while (true);

        return trees;
    }

    @Transactional
    public void execute() {

        Map<String, ProductNode> productMap = new HashMap<>();
        Map<String, ContentNode> contentMap = new HashMap<>();



        LOG.infof("%s SYNCING WITH DATA SOURCE: %s", this, this.productDataSource);

        // fetch tree(s)
        LOG.infof("FETCHING PRODUCT TREES FOR FILTER: %s", this.productOids);
        Map<String, UpstreamProductTree> upstreamProductTrees = !this.productOids.isEmpty() ?
            this.resolveUpstreamProductTrees(this.productOids) :
            this.productDataSource.getProductTrees();

        LOG.debugf("FETCHED %d RELATED PRODUCT TREES: %s", upstreamProductTrees.size(), upstreamProductTrees.keySet());

        // fetch upstream product definitions
        LOG.infof("FETCHING UPSTREAM PRODUCTS");
        Map<String, UpstreamProduct> upstreamProducts = this.productDataSource
            .getProducts(upstreamProductTrees.keySet());

        LOG.infof("FETCHED %d UPSTREAM PRODUCT DEFINITIONS", upstreamProducts.size());

        LOG.infof("MAPPING UPSTREAM PRODUCTS...");
        this.populateExtProductNodes(productMap, contentMap, upstreamProducts.values());

        LOG.infof("MAPPING UPSTREAM PRODUCT TREES...");
        this.populateExtProductTrees(productMap, upstreamProductTrees.values());

        LOG.infof("FETCHING LOCAL PRODUCT DEFINITIONS...");
        LOG.infof("FETCHING RELATED OIDS OF UPSTREAM PRODUCT TREES...");

        // Set<String> localOids = this.productCurator.getRelatedOidsByOids(upstreamProductTrees.keySet());
        LOG.info("FETCHING LOCAL PRODUCT GRAPH...");
        Set<ProductOidGraphNode> localProductGraph = this.productCurator
            .getProductOidRelationsByOids(upstreamProductTrees.keySet());
        LOG.infof("FETCHED %d PRODUCT GRAPH ENTIRES", localProductGraph.size());

        Set<String> localOids = new HashSet<>();
        localProductGraph.forEach(elem -> {
            localOids.add(elem.productOid());
            localOids.add(elem.childOid());
        });
        LOG.infof("PARSED %d LOCAL OIDS", localOids.size());

        LOG.infof("FETCHING %d LOCAL PRODUCTS...", localOids.size());
        Map<String, Product> localProducts = this.productCurator.getProductsByOids(localOids);
        LOG.infof("FETCHED %d LOCAL PRODUCTS...", localProducts.size());

        LOG.info("POPULATING PRODUCT NODES WITH LOCAL DATA...");
        this.populateLocalProductNodes(productMap, localProducts.values());
        this.populateLocalProductLinks(productMap, localProductGraph);

        LOG.info("FETCHING LOCAL CONTENT...");
        Map<String, Content> localContent = this.productCurator.getContentByProductOids(localOids);
        LOG.infof("FETCHED %d LOCAL CONTENTS...", localContent.size());

        LOG.info("POPULATING CONTENT NODES WITH LOCAL DATA...");
        this.populateLocalContentNodes(contentMap, localContent.values());

        LOG.info("FETCHING LOCAL PRODUCT-CONTENT LINKS...");
        Set<ProductContentOidLink> pcLinks = this.productCurator.getProductContentOidLinksByProductOids(localOids);
        LOG.infof("FETCHED %d LOCAL PRODUCT-CONTENT LINKS", pcLinks.size());

        LOG.info("MAPPING LOCAL PRODUCT-CONTENT LINKS...");
        this.populateLocalContentLinks(productMap, contentMap, pcLinks);

        LOG.info("PROCESSING NODES...");
        LOG.info("  PROCESSING PRODUCT NODES...");
        productMap.values().forEach(GraphElement::process);

        LOG.info("");
        LOG.info("  PROCESSING CONTENT NODES...");
        contentMap.values().forEach(GraphElement::process);


        // apply changes
        LOG.info("");
        LOG.info("WALKING NODE GRAPH...");
        PersistenceVisitor visitor = this.visitorProvider.get();
        Stack<GraphElement> path = new Stack<GraphElement>();
        productMap.values().forEach(elem -> elem.walk(path, visitor));
        contentMap.values().forEach(elem -> elem.walk(path, visitor));

        LOG.infof("WALK COMPLETE. COUNTS: %s", visitor.getCounts());

        LOG.info("COMMITTING TRANSACTION...");
    }




// select
//   table_name,
//   (xpath('/row/c/text()', query_to_xml(format('select count(*) as c from %I.%I', table_schema, TABLE_NAME), FALSE, TRUE, '')))[1]::text::int AS row_count,
//   pg_size_pretty(pg_relation_size(quote_ident(table_name))),
//   pg_relation_size(quote_ident(table_name))
// from information_schema.tables
// where table_schema = 'public'
// order by pg_relation_size desc;

// SELECT prod.oid AS "random_product_oid", child.oid, pgraph.type AS "inheritance_type", pgraph.depth, content.oid AS "accessible_content_oid"
//     FROM (SELECT * FROM gv_products ORDER BY RANDOM() LIMIT 1) prod
//     JOIN gv_product_graph pgraph ON prod.id = pgraph.product_id
//     JOIN gv_product_contents pclink ON pclink.product_id = pgraph.child_product_id
//     JOIN gv_products child ON child.id = pgraph.child_product_id -- optional, only here for demonstration purposes
//     JOIN gv_contents content ON content.id = pclink.content_id;


}
