package com.redhat.graviton.sync.graph;






public abstract class GraphLink<T extends GraphLink, P extends GraphNode<? extends GraphNode, ?, ?>,
    C extends GraphNode<? extends GraphNode, ?, ?>> implements GraphElement<T> {

    private final P parentNode;
    private final C childNode;

    private GraphElement.State state;

    public GraphLink(P parentNode, C childNode) {
        if (parentNode == null) {
            throw new IllegalArgumentException("parent node is null");
        }

        if (childNode == null) {
            throw new IllegalArgumentException("child node is null");
        }

        this.parentNode = parentNode;
        this.childNode = childNode;

        this.state = GraphElement.State.UNPROCESSED;
    }

    public GraphElement.State getState() {
        return this.state;
    }

    public T setState(GraphElement.State state) {
        if (state == null) {
            throw new IllegalArgumentException("state is null");
        }

        this.state = state;
        return (T) this;
    }

    public P getParentNode() {
        return this.parentNode;
    }

    public C getChildNode() {
        return this.childNode;
    }

    public GraphElement.State getChildState() {
        return this.childNode.getState();
    }

}
