package com.redhat.graviton.db.model;

public record ProductOidGraphNode(String productOid, String childOid, int depth, String type) { }
