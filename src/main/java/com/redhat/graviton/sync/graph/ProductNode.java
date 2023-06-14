package com.redhat.graviton.sync.graph;

import com.redhat.graviton.db.model.Product;
import com.redhat.graviton.api.datasource.model.ExtProduct;
import com.redhat.graviton.sync.NodeUpdates;

import org.jboss.logging.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;



public class ProductNode extends GraphNode<ProductNode, Product, ExtProduct> {
    private static final Logger LOG = Logger.getLogger(ProductNode.class);

    private Set<GraphElement> parents;

    private Map<String, ProductLink> childrenProducts;
    private Map<String, ContentLink> childrenContent;

    private NodeUpdates updates;

    public ProductNode() {
        this.parents = new HashSet<>();

        this.childrenProducts = new HashMap<>();
        this.childrenContent = new HashMap<>();
    }

    public Set<GraphElement> getParents() {
        return this.parents;
    }

    public ProductNode addParent(GraphElement element) {
        if (element == null) {
            throw new IllegalArgumentException("element is null");
        }

        this.parents.add(element);
        return this;
    }

    public ProductLink getProductLink(String oid) {
        return this.childrenProducts.get(oid);
    }

    public Collection<ProductLink> getChildrenProductLinks() {
        return this.childrenProducts.values();
    }

    public ProductNode addProductLink(String oid, ProductLink link) {
        if (oid == null || link == null) {
            throw new IllegalArgumentException();
        }

        this.childrenProducts.put(oid, link);
        return this;
    }

    public ContentLink getContentLink(String oid) {
        return this.childrenContent.get(oid);
    }

    public ProductNode addContentLink(String oid, ContentLink link) {
        if (oid == null || link == null) {
            throw new IllegalArgumentException();
        }

        this.childrenContent.put(oid, link);
        return this;
    }


    public NodeUpdates getNodeUpdates() {
        return this.updates;
    }

    public void walk(Stack<GraphElement> path, Visitor visitor) {
        path.push(this);

        // We have to visit this node first before we can hit our children
        // nodes to ensure this node is persisted and has an ID before our
        // children try to link back to it.
        // This logic is reversed for links, which will visit children first.
        visitor.visit(path, this);

        this.childrenProducts.values()
            .forEach(link -> link.walk(path, visitor));

        this.childrenContent.values()
            .forEach(link -> link.walk(path, visitor));

        path.pop();
    }

    public void process() {
        // - if we have a local entity:
        //   - if we have an upstream entity:
        //     - check for changes (or force?)
        //     - if change detected:
        //       - set state to UPDATE
        //       - catalog changes with a NodeUpdates object
        //     - else:
        //       - set state to UNCHANGED
        //       - clear any existing NodeUpdates object
        //   - else:
        //     - flag entity for potential removal
        // - else:
        //   - if we have an upstream entity:
        //     - set state to CREATED
        //     - clear any existing NodeUpdates object and replace with a special
        //       updates object which reports all fields as updated (for completion, probably not in demo)

        if (this.getState() != GraphElement.State.UNPROCESSED) {
            LOG.debugf("Node already processed. Node: %s => %s", this, this.getState());
            return;
        }

        // Process children first
        this.childrenProducts.values()
            .forEach(ProductLink::process);

        this.childrenContent.values()
            .forEach(ContentLink::process);

        LOG.debugf("Processing node: %s", this);

        Product local = this.getLocalEntity();
        ExtProduct upstream = this.getExternalEntity();

        if (local != null) {
            if (upstream != null) {
                this.updates = this.applyChanges(local, upstream);
                this.setState(this.updates.count() > 0 ? GraphElement.State.UPDATED : GraphElement.State.UNCHANGED);
            }
            else {
                // POTENTIALLY SAFE TO DELETE?? Need to verify that entity has no parents which are
                // not also being deleted. Also not going to be part of the demo.
                this.setState(GraphElement.State.DELETED);
                this.updates = null;
            }
        }
        else if (upstream != null) {
            // CREATING A NEW LOCAL INSTANCE!
            local = this.createLocalEntity(upstream);
            this.applyChanges(local, upstream);
            this.setLocalEntity(local);

            this.setState(GraphElement.State.CREATED);
            this.updates = null;
        }
        else {
            throw new IllegalStateException("Node has no entities?");
        }
    }

    private Product createLocalEntity(ExtProduct source) {
        Product product = new Product()
            .setOid(source.getId());

        return product;
    }

    private NodeUpdates applyChanges(Product local, ExtProduct upstream) {
        // This almost seems like having both objects in JSON would be far easier than class-level
        // field comparison (even with some reflection jank). Downside would be ensuring the objects
        // have the same field names to begin with...

        // We're following the CP model of "null = no change, except when it doesn't" for
        // determining changes

        NodeUpdates updates = new NodeUpdates();

        // these are dates for the node lifecycle, we currently don't care about them
        // should probably be removed from the upstream/external definition
            // upstream.getCreated()
            // upstream.getUpdated()

        // OID cannot change
            // upstream.getId()

        String name = upstream.getName();
        if (name != null && !name.equals(local.getName())) {
            updates.addField("name");
            local.setName(name);
        }

        Integer multiplier = upstream.getMultiplier();
        if (multiplier != null && !multiplier.equals(local.getMultiplier())) {
            updates.addField("multiplier");
            local.setMultiplier(multiplier);
        }

        Map<String, String> attributes = upstream.getAttributes();
        if (attributes != null && !attributes.equals(local.getAttributes())) {
            updates.addField("attributes");
            local.setAttributes(attributes);
        }

        Set<String> dependentProductOids = upstream.getDependentProductIds();
        if (dependentProductOids != null && !dependentProductOids.equals(local.getDependentProductOids())) {
            updates.addField("dependentProductOids");
            local.setDependentProductOids(dependentProductOids);
        }

        // Content and children products will be handled by the rest of the graph

        return updates;
    }

    public String toString() {
        boolean local = this.getLocalEntity() != null;
        boolean ext = this.getExternalEntity() != null;

        String oid = local ? this.getLocalEntity().getOid() : this.getExternalEntity().getId();

        return String.format("ProductNode [oid: %s, local: %s, ext: %s]", oid, local, ext);
    }

}
