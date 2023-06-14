package com.redhat.graviton.impl.datasource.fs.model;

import com.redhat.graviton.api.datasource.model.ExtProductChildren;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;



public class FileSystemExtProductChildren implements ExtProductChildren {

    private String productId;
    private Map<String, Set<String>> childrenProductIds;


    public FileSystemExtProductChildren() {
        this.childrenProductIds = new HashMap<>();
    }

    public String getProductId() {
        return this.productId;
    }

    public FileSystemExtProductChildren setProductId(String productId) {
        this.productId = productId;
        return this;
    }

    public Set<String> getChildrenProductIds(String type) {
        return this.childrenProductIds.get(type);
    }

    public Map<String, Set<String>> getChildrenProductIds() {
        return this.childrenProductIds;
    }

    public FileSystemExtProductChildren addChildrenProductIds(String type,
        Collection<String> childrenProductIds) {

        if (childrenProductIds == null || childrenProductIds.isEmpty()) {
            return this;
        }

        this.childrenProductIds.computeIfAbsent(type, key -> new HashSet<>())
            .addAll(childrenProductIds);

        return this;
    }

}
