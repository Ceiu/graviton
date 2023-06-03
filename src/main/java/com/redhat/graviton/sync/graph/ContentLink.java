package com.redhat.graviton.sync.graph;

import org.jboss.logging.Logger;

import java.util.Stack;



public class ContentLink extends GraphLink<ContentLink, ProductNode, ContentNode> {
    private static final Logger LOG = Logger.getLogger(ContentLink.class);

    // This seems generic enough that it only needs typing for usage, and Java's generics will
    // probably fight me as soon as I go to run this. Likely no need for typed link objects.

    private String type; // derived vs provided
    private String oid;  // entity oid

    private boolean localLinkPresent;
    private boolean externalLinkPresent;

    public ContentLink(ProductNode parent, ContentNode child) {
        super(parent, child);
    }

    public ContentLink setLocalLinkPresence(boolean present) {
        this.localLinkPresent = present;
        return this;
    }

    public boolean isLocalLinkPresent() {
        return this.localLinkPresent;
    }

    public ContentLink setExternalLinkPresence(boolean present) {
        this.externalLinkPresent = present;
        return this;
    }

    public boolean isExternalLinkPresent() {
        return this.externalLinkPresent;
    }

    @Override
    public void walk(Stack<GraphElement> path, Visitor visitor) {
        path.push(this);

        this.getChildNode().walk(path, visitor);
        visitor.visit(path, this);

        path.pop();
    }

    @Override
    public void process() {
        if (this.getState() != GraphElement.State.UNPROCESSED) {
            LOG.debugf("Node already processed. Node: %s => %s", this, this.getState());
            return;
        }

        // process children first
        this.getChildNode().process();

        LOG.debugf("Processing node: %s", this);

        boolean localLinkPresent = this.isLocalLinkPresent();
        boolean externalLinkPresent = this.isExternalLinkPresent();

        // Local Extern
        //   T     T    - UNCHANGED (possibly CHILDREN_UPDATED)
        //   T     F    - DELETED
        //   F     T    - CREATED
        //   F     F    - ERROR STATE

        if (localLinkPresent) {
            this.setState(externalLinkPresent ? GraphElement.State.UNCHANGED : GraphElement.State.DELETED);
        }
        else if (externalLinkPresent) {
            this.setState(GraphElement.State.CREATED);
        }
        else {
            throw new IllegalStateException("Invalid link state");
        }
    }

    public String toString() {
        return String.format("ContentLink [%s => %s]", this.getParentNode(), this.getChildNode());
    }
}
