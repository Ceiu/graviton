package com.redhat.graviton.sync.graph;

import java.util.Stack;




public abstract class GraphNode<T extends GraphNode, L, E> implements GraphElement<T> {

    private L localEntity;
    private E externalEntity;

    private GraphElement.State state = GraphElement.State.UNPROCESSED;

    // If the node is dirty -- indicating we should update everything regardless of what changes
    // we detect. Useful for repairing borked data, but not required for the poc.
    private boolean dirty;

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

    public boolean isDirty() {
        return this.dirty;
    }

    public T setDirty(boolean dirty) {
        this.dirty = dirty;
        return (T) this;
    }

    public L getLocalEntity() {
        return this.localEntity;
    }

    public T setLocalEntity(L entity) {
        this.localEntity = entity;
        return (T) this;
    }

    public E getExternalEntity() {
        return this.externalEntity;
    }

    public T setExternalEntity(E entity) {
        this.externalEntity = entity;
        return (T) this;
    }

}
