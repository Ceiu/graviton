package com.redhat.graviton.sync.graph;

import com.redhat.graviton.db.model.*;
import com.redhat.graviton.api.datasource.model.*;
import com.redhat.graviton.sync.NodeUpdates;

import org.jboss.logging.Logger;

import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;




public class SubscriptionNode extends GraphNode<SubscriptionNode, Subscription, ExtSubscription> {
    private static final Logger LOG = Logger.getLogger(SubscriptionNode.class);

    private final Organization org;
    private final Map<String, Product> productMap;

    private Set<GraphElement> parents;

    private NodeUpdates updates;

    public SubscriptionNode(Organization org, Map<String, Product> productMap) {
        if (org == null) {
            throw new IllegalArgumentException("org is null");
        }

        if (productMap == null) {
            throw new IllegalArgumentException("hack map not provided");
        }

        this.org = org;
        this.productMap = productMap;

        this.parents = new HashSet<>();
    }

    public Set<GraphElement> getParents() {
        return this.parents;
    }

    public SubscriptionNode addParent(GraphElement element) {
        if (element == null) {
            throw new IllegalArgumentException("element is null");
        }

        this.parents.add(element);
        return this;
    }

    public NodeUpdates getNodeUpdates() {
        return this.updates;
    }

    public void walk(Stack<GraphElement> path, Visitor visitor) {
        path.push(this);
        visitor.visit(path, this);
        path.pop();
    }

    public void process() {
        if (this.getState() != GraphElement.State.UNPROCESSED) {
            LOG.debugf("Node already processed. Node: %s => %s", this, this.getState());
            return;
        }

        LOG.debugf("Processing node: %s", this);

        Subscription local = this.getLocalEntity();
        ExtSubscription upstream = this.getExternalEntity();

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


    private Subscription createLocalEntity(ExtSubscription source) {
        Subscription content = new Subscription()
            .setOid(source.getId())
            .setOrganization(this.org);

        return content;
    }

    private NodeUpdates applyChanges(Subscription local, ExtSubscription upstream) {
        // This almost seems like having both objects in JSON would be far easier than class-level
        // field comparison (even with some reflection jank). Downside would be ensuring the objects
        // have the same field names to begin with...

        // We're following the CP model of "null = no change, except when it doesn't" for
        // determining changes. Fix this with something better in the actual release.

        NodeUpdates updates = new NodeUpdates();

        // these are dates for the node lifecycle, we currently don't care about them
        // should probably be removed from the upstream/external definition
            // upstream.getCreated()
            // upstream.getUpdated()

        // OID cannot change
            // upstream.getId()

        // Organization probably shouldn't change

        // handle the product link separately, even though it's just an OID comparison...? Hrmm...
        // This is so disgustingly hack. Come up with a far more elegant solution to mapping the OID back to a product

        String productOid = upstream.getProductId();
        if (productOid == null) {
            throw new IllegalStateException("malformed subscription data: no product defined");
        }

        Product upstreamProduct = this.productMap.get(productOid);
        if (upstreamProduct == null) {
            // This shouldn't happen with our goofy hack
            throw new IllegalStateException("subscription references a product that doesn't exist: " + productOid);
        }

        Product localProduct = local.getProduct();
        if (localProduct == null || !productOid.equals(localProduct.getOid())) {
            updates.addField("product");
            local.setProduct(upstreamProduct);
        }


        // I wouldn't expect type to change, either, but it could, I guess...
        String type = upstream.getType();
        if (type != null && !type.equals(local.getType())) {
            updates.addField("type");
            local.setType(type);
        }

        Instant startDate = upstream.getStartDate();
        if (startDate != null && !startDate.equals(local.getStartDate())) {
            updates.addField("startDate");
            local.setStartDate(startDate);
        }

        Instant endDate = upstream.getEndDate();
        if (endDate != null && !endDate.equals(local.getEndDate())) {
            updates.addField("endDate");
            local.setEndDate(endDate);
        }

        String contractNumber = upstream.getContractNumber();
        if (contractNumber != null && !contractNumber.equals(local.getContractNumber())) {
            updates.addField("contractNumber");
            local.setContractNumber(contractNumber);
        }

        String accountNumber = upstream.getAccountNumber();
        if (accountNumber != null && !accountNumber.equals(local.getAccountNumber())) {
            updates.addField("accountNumber");
            local.setAccountNumber(accountNumber);
        }

        String orderNumber = upstream.getOrderNumber();
        if (orderNumber != null && !orderNumber.equals(local.getOrderNumber())) {
            updates.addField("orderNumber");
            local.setOrderNumber(orderNumber);
        }

        return updates;
    }

    public String toString() {
        boolean local = this.getLocalEntity() != null;
        boolean ext = this.getExternalEntity() != null;

        String oid = local ? this.getLocalEntity().getOid() : this.getExternalEntity().getId();

        return String.format("SubscriptionNode [oid: %s, local: %s, ext: %s]", oid, local, ext);
    }
}
