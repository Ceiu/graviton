package com.redhat.graviton.sync.graph;

import java.util.Stack;




public interface GraphElement<T extends GraphElement> {

    public static enum State {
        UNPROCESSED,
        CREATED,
        UNCHANGED,
        CHILDREN_UPDATED,
        UPDATED,
        DELETED
    }

    public State getState();

    public T setState(State state);

    public void walk(Stack<GraphElement> path, Visitor visitor);

    public void process();

}
