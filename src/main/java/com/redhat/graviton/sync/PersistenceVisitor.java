package com.redhat.graviton.sync;

import com.redhat.graviton.api.datasource.ProductDataSource;
import com.redhat.graviton.db.curators.*;
import com.redhat.graviton.db.model.*;
import com.redhat.graviton.api.datasource.model.*;
import com.redhat.graviton.sync.graph.*;

import java.util.Set;
import java.util.HashSet;

import org.jboss.logging.Logger;

import jakarta.inject.Inject;
import jakarta.enterprise.context.Dependent;

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
public class PersistenceVisitor implements Visitor {
    private static final Logger LOG = Logger.getLogger(PersistenceVisitor.class);

    private final ProductCurator productCurator;
    private final ContentCurator contentCurator;
    private final SubscriptionCurator subscriptionCurator;

    private Set<GraphElement> visited;

    private Map<GraphElement.State, Integer> counts;



    @Inject
    public PersistenceVisitor(ProductCurator productCurator, ContentCurator contentCurator,
        SubscriptionCurator subscriptionCurator) {

        this.productCurator = Objects.requireNonNull(productCurator);
        this.contentCurator = Objects.requireNonNull(contentCurator);
        this.subscriptionCurator = Objects.requireNonNull(subscriptionCurator);

        this.visited = new HashSet<>();
        this.counts = new HashMap<>();
    }

    public Map<GraphElement.State, Integer> getCounts() {
        return this.counts;
    }

    @Override
    public void visit(Stack<GraphElement> path, GraphElement elem) {
        // Skip elements we've already seen.
        if (this.visited.contains(elem)) {
            return;
        }

        // TODO: Ensure we don't have a cycle? Probably should.

        // TODO: FIXME: Make this more elegant. It's not 1980, and if/else chains are boring.
        if (elem instanceof ProductNode) {
            this.visitProductNode((ProductNode) elem);
        }
        else if (elem instanceof ContentNode) {
            this.visitContentNode((ContentNode) elem);
        }
        else if (elem instanceof ProductLink) {
            this.visitProductLink((ProductLink) elem);
        }
        else if (elem instanceof ContentLink) {
            this.visitContentLink((ContentLink) elem);
        }
        else if (elem instanceof SubscriptionNode) {
            this.visitSubscriptionNode((SubscriptionNode) elem);
        }

        this.counts.compute(elem.getState(), (key, value) -> value == null ? 1 : value + 1);
        this.visited.add(elem);
    }


    private void createProductGraphEntry(Product parent, Product child, int depth, String type) {
        ProductGraphNode pgn = new ProductGraphNode()
            .setProductId(parent.getId())
            .setChildProductId(child.getId())
            .setDepth(depth)
            .setType(type);

        LOG.debugf("CREATING PRODUCT GRAPH ENTRY: %s", pgn);
        this.productCurator.persistGraphNode(pgn);
    }

    private void removeProductGraphEntry(Product parent, Product child, int depth) {
        ProductGraphNode pgn = new ProductGraphNode()
            .setProductId(parent.getId())
            .setChildProductId(child.getId())
            .setDepth(depth);

        this.productCurator.removeGraphNode(pgn);
    }

    public void visitProductNode(ProductNode node) {
        LOG.debugf("VISITING PRODUCT NODE: %s", node);

        switch (node.getState()) {
            case CREATED:
                this.productCurator.persist(node.getLocalEntity());
                this.createProductGraphEntry(node.getLocalEntity(), node.getLocalEntity(), 0, "self");
                break;

            case UPDATED:
                // Technically nothing should need to be done here, since Hibernate is going to magic
                // commit for us by default? I hate this I hate this I hate this I hate this
                // LOG.infof("UPDATED?? %s => %s", node, node.getNodeUpdates());
                break;

            case UNCHANGED:
                // Nothing to do -- node was unchanged
                break;

            case DELETED:
                LOG.debugf("NODE FLAGGED FOR DELETION, IGNORING FOR POC");
                break;

            default:
                throw new IllegalStateException("unexpected node state: " + node.getState());
        }
    }


    private void createProductLinks(ProductNode pnode, ProductLink link, int depth, String type) {
        Product parent = pnode.getLocalEntity();

        ProductNode cnode = link.getChildNode();
        Product child = cnode.getLocalEntity();

        if (parent.getId() == null) {
            LOG.warnf("PARENT ENTITY HAS NO ID? %s => %s (%s)", parent, parent.getId(), parent.getOid());
            LOG.warnf("PARENT NODE VISITED YET? %s", this.visited.contains(parent));
            throw new IllegalStateException("NO NO NO NO NO");
        }


        this.createProductGraphEntry(parent, child, depth, type);

        // Traverse the grand children and recursively create our graph
        depth = depth + 1;
        for (ProductLink cplink : cnode.getChildrenProductLinks()) {
            this.createProductLinks(pnode, cplink, depth, type);
        }
    }

    private void removeProductLinks(ProductNode pnode, ProductLink link, int depth) {
        Product parent = pnode.getLocalEntity();

        ProductNode cnode = link.getChildNode();
        Product child = cnode.getLocalEntity();

        this.removeProductGraphEntry(parent, child, depth);

        // Traverse the grand children and recursively create our graph
        depth = depth + 1;
        for (ProductLink cplink : cnode.getChildrenProductLinks()) {
            this.removeProductLinks(pnode, cplink, depth);
        }
    }


    public void visitProductLink(ProductLink link) {
        LOG.debugf("VISITING PRODUCT LINK: %s", link);

        // TODO: be a bit smarter about what kind of parent we have.
        ProductNode parent = (ProductNode) link.getParentNode();

        switch (link.getState()) {
            case CREATED:
                this.createProductLinks(parent, link, 1, link.getType());
                break;

            case DELETED:
                this.removeProductLinks(parent, link, 1);
                break;

            case UNCHANGED:
                // Nothing to do -- link was unchanged
                break;

            default:
                throw new IllegalStateException("unexpected link state: " + link.getState());
        }
    }

    public void visitContentNode(ContentNode node) {
        LOG.debugf("VISITING CONTENT NODE: %s", node);

        switch (node.getState()) {
            case CREATED:
                this.contentCurator.persist(node.getLocalEntity());
                break;

            case UPDATED:
                // Technically nothing should need to be done here, since Hibernate is going to magic
                // commit for us by default? I hate this I hate this I hate this I hate this
                // LOG.infof("UPDATED?? %s => %s", node, node.getNodeUpdates());
                break;

            case UNCHANGED:
                // Nothing to do -- node was unchanged
                break;

            case DELETED:
                LOG.debugf("NODE FLAGGED FOR DELETION, IGNORING FOR POC");
                break;

            default:
                throw new IllegalStateException("unexpected node state: " + node.getState());
        }
    }

    private void createContentLink(ProductNode parent, ContentLink link) {
        Product product = parent.getLocalEntity();
        Content content = link.getChildNode().getLocalEntity();

        ProductContent pclink = new ProductContent()
            .setProductId(product.getId())
            .setContentId(content.getId());

        this.productCurator.persistProductContentLink(pclink);
    }

    private void removeContentLink(ProductNode parent, ContentLink link) {
        Product product = parent.getLocalEntity();
        Content content = link.getChildNode().getLocalEntity();

        ProductContent pclink = new ProductContent()
            .setProductId(product.getId())
            .setContentId(content.getId());

        this.productCurator.removeProductContentLink(pclink);
    }

    private void visitContentLink(ContentLink link) {
        LOG.debugf("VISITING CONTENT LINK: %s", link);

        // TODO: be a bit smarter about what kind of parent we have.
        ProductNode parent = (ProductNode) link.getParentNode();

        switch (link.getState()) {
            case CREATED:
                this.createContentLink(parent, link);
                break;

            case DELETED:
                this.removeContentLink(parent, link);
                break;

            case UNCHANGED:
                // Nothing to do -- link was unchanged
                break;

            default:
                throw new IllegalStateException("unexpected link state: " + link.getState());
        }
    }

    private void visitSubscriptionNode(SubscriptionNode node) {
        LOG.debugf("VISITING SUBSCRIPTION NODE: %s", node);

        switch (node.getState()) {
            case CREATED:
                this.subscriptionCurator.persist(node.getLocalEntity());
                break;

            case UPDATED:
                // Technically nothing should need to be done here, since Hibernate is going to magic
                // commit for us by default? I hate this I hate this I hate this I hate this
                LOG.infof("UPDATED?? %s => %s", node, node.getNodeUpdates());
                break;

            case UNCHANGED:
                // Nothing to do -- node was unchanged
                break;

            case DELETED:
                LOG.debugf("NODE FLAGGED FOR DELETION, IGNORING FOR POC");
                break;

            default:
                throw new IllegalStateException("unexpected node state: " + node.getState());
        }
    }


}
