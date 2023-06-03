package com.redhat.graviton.sync.graph;

import java.util.Stack;




public interface Visitor {

    public void visit(Stack<GraphElement> path, GraphElement elem);

}
