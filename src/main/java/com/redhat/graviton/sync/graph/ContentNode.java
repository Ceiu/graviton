package com.redhat.graviton.sync.graph;

import com.redhat.graviton.db.model.Content;
import com.redhat.graviton.api.datasource.model.UpstreamContent;
import com.redhat.graviton.sync.NodeUpdates;

import org.jboss.logging.Logger;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;




public class ContentNode extends GraphNode<ContentNode, Content, UpstreamContent> {
    private static final Logger LOG = Logger.getLogger(ContentNode.class);

    private Set<GraphElement> parents;

    private NodeUpdates updates;

    public ContentNode() {
        this.parents = new HashSet<>();
    }

    public Set<GraphElement> getParents() {
        return this.parents;
    }

    public ContentNode addParent(GraphElement element) {
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

        Content local = this.getLocalEntity();
        UpstreamContent upstream = this.getExternalEntity();

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


    private Content createLocalEntity(UpstreamContent source) {
        Content content = new Content()
            .setOid(source.getId());

        return content;
    }

    private NodeUpdates applyChanges(Content local, UpstreamContent upstream) {
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

        String type = upstream.getType();
        if (type != null && !type.equals(local.getType())) {
            updates.addField("type");
            local.setType(type);
        }

        String label = upstream.getLabel();
        if (label != null && !label.equals(local.getLabel())) {
            updates.addField("label");
            local.setLabel(label);
        }

        String name = upstream.getName();
        if (name != null && !name.equals(local.getName())) {
            updates.addField("name");
            local.setName(name);
        }

        String vendor = upstream.getVendor();
        if (vendor != null && !vendor.equals(local.getVendor())) {
            updates.addField("vendor");
            local.setVendor(vendor);
        }

        String contentUrl = upstream.getContentUrl();
        if (contentUrl != null && !contentUrl.equals(local.getContentUrl())) {
            updates.addField("contentUrl");
            local.setContentUrl(contentUrl);
        }

        String gpgUrl = upstream.getGpgUrl();
        if (gpgUrl != null && !gpgUrl.equals(local.getGpgUrl())) {
            updates.addField("gpgUrl");
            local.setGpgUrl(gpgUrl);
        }

        String requiredTags = upstream.getRequiredTags();
        if (requiredTags != null && !requiredTags.equals(local.getRequiredTags())) {
            updates.addField("requiredTags");
            local.setRequiredTags(requiredTags);
        }

        String arches = upstream.getArches();
        if (arches != null && !arches.equals(local.getArches())) {
            updates.addField("arches");
            local.setArches(arches);
        }

        String releaseVersion = upstream.getReleaseVer();
        if (releaseVersion != null && !releaseVersion.equals(local.getReleaseVersion())) {
            updates.addField("releaseVersion");
            local.setReleaseVersion(releaseVersion);
        }

        Integer metadataExpiration = upstream.getMetadataExpiration();
        if (metadataExpiration != null && !metadataExpiration.equals(local.getMetadataExpiration())) {
            updates.addField("metadataExpiration");
            local.setMetadataExpiration(metadataExpiration);
        }

        Set<String> requiredProductIds = upstream.getRequiredProductIds();
        if (requiredProductIds != null && !requiredProductIds.equals(local.getRequiredProductOids())) {
            updates.addField("requiredProductIds");
            local.setRequiredProductOids(requiredProductIds);
        }

        Boolean enabled = upstream.getEnabled();
        if (enabled != null && !enabled.equals(local.getEnabled())) {
            updates.addField("enabled");
            local.setEnabled(enabled);
        }

        return updates;
    }

    public String toString() {
        boolean local = this.getLocalEntity() != null;
        boolean ext = this.getExternalEntity() != null;

        String oid = local ? this.getLocalEntity().getOid() : this.getExternalEntity().getId();

        return String.format("ContentNode [oid: %s, local: %s, ext: %s]", oid, local, ext);
    }
}
