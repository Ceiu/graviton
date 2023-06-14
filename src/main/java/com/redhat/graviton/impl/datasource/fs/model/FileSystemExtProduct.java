package com.redhat.graviton.impl.datasource.fs.model;

import com.redhat.graviton.api.datasource.model.ExtContent;
import com.redhat.graviton.api.datasource.model.ExtProduct;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


public class FileSystemExtProduct implements ExtProduct {

    private String id;
    private String name;
    private Integer multiplier;
    private Map<String, String> attributes;
    private Set<String> dependentProductIds;
    private List<FileSystemExtContent> content;

    public FileSystemExtProduct() {
        // intentionally left empty
    }

    @Override
    public String getId() {
        return this.id;
    }

    public FileSystemExtProduct setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public FileSystemExtProduct setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public Integer getMultiplier() {
        return this.multiplier;
    }

    public FileSystemExtProduct setMultiplier(Integer multiplier) {
        this.multiplier = multiplier;
        return this;
    }

    @Override
    public Map<String, String> getAttributes() {
        return this.attributes;
    }

    public FileSystemExtProduct setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
        return this;
    }

    @Override
    public Set<String> getDependentProductIds() {
        return this.dependentProductIds;
    }

    public FileSystemExtProduct setDependentProductIds(Set<String> dependentProductIds) {
        this.dependentProductIds = dependentProductIds;
        return this;
    }

    @Override
    public List<FileSystemExtContent> getContent() {
        return this.content;
    }

    public FileSystemExtProduct setContent(List<FileSystemExtContent> content) {
        this.content = content;
        return this;
    }

    public String toString() {
        List<? extends ExtContent> content = this.getContent();
        int contentCount = content != null ? content.size() : 0;

        return String.format("FileSystemExtProduct [id: %s, name: %s, content: %d]", this.getId(),
            this.getName(), contentCount);
    }

}
