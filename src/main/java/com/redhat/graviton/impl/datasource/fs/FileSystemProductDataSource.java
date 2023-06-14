package com.redhat.graviton.api.datasource.impl;

import com.redhat.graviton.api.datasource.ProductDataSource;
import com.redhat.graviton.api.datasource.model.ExtProduct;
import com.redhat.graviton.api.datasource.model.ExtProductChildren;
import com.redhat.graviton.impl.datasource.fs.model.FileSystemExtProduct;
import com.redhat.graviton.impl.datasource.fs.model.FileSystemExtProductChildren;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.jboss.logging.Logger;

import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.File;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


// This file exists for demo purposes only. Don't ship this, and you probably shouldn't even be
// looking at it. Bad code is bad.

@ApplicationScoped
public class FileSystemProductDataSource implements ProductDataSource {
    private static final Logger LOG = Logger.getLogger(FileSystemProductDataSource.class);

    private static final String BASE_PATH = "/home/crog/devel/subscription_data/product_data";

    private final ObjectMapper mapper;

    @Inject
    public FileSystemProductDataSource(ObjectMapper mapper) {
        this.mapper = Objects.requireNonNull(mapper);
    }

    private Map<String, ExtProduct> getProductsImpl(Set<String> oidFilter) {

        File dir = new File(BASE_PATH);
        if (!dir.canRead() || !dir.isDirectory()) {
            throw new IllegalStateException("BASE_PATH is not readable or not a directory: " + BASE_PATH);
        }

        Map<String, ExtProduct> products = new HashMap<>();
        TypeReference<FileSystemExtProduct> typeref = new TypeReference<FileSystemExtProduct>() {};

        File[] jsonFiles = dir.listFiles((loc, name) -> {
            boolean match = name != null && name.endsWith("-data.json");

            if (match && oidFilter != null) {
                String oid = name.substring(0, name.length() - 10);
                match = oidFilter.contains(oid);
            }

            return match;
        });

        if (jsonFiles == null) {
            throw new IllegalStateException("jsonFiles is null...?");
        }

        LOG.debugf("Found %d matching files", jsonFiles.length);
        for (File file : jsonFiles) {
            LOG.debugf("Reading file %s", file);

            try {
                FileSystemExtProduct deserialized = this.mapper.readValue(file, typeref);
                products.put(deserialized.getId(), deserialized);
            }
            catch (Exception e) {
                throw new RuntimeException("Could not read from file " + file + ": " + e.getMessage(), e);
            }
        }

        return products;
    }

    private Map<String, ExtProductChildren> getProductChildrenImpl(Set<String> oidFilter) {

        File dir = new File(BASE_PATH);
        if (!dir.canRead() || !dir.isDirectory()) {
            throw new IllegalStateException("BASE_PATH is not readable or not a directory: " + BASE_PATH);
        }

        Map<String, ExtProductChildren> trees = new HashMap<>();
        TypeReference<FileSystemExtProductChildren> typeref = new TypeReference<FileSystemExtProductChildren>() {};

        File[] jsonFiles = dir.listFiles((loc, name) -> {
            boolean match = name != null && name.endsWith("-children.json");

            if (match && oidFilter != null) {
                String oid = name.substring(0, name.length() - 10);
                match = oidFilter.contains(oid);
            }

            return match;
        });

        if (jsonFiles == null) {
            throw new IllegalStateException("jsonFiles is null...?");
        }

        LOG.debugf("Found %d matching files", jsonFiles.length);
        for (File file : jsonFiles) {
            LOG.debugf("Reading file %s", file);

            try {
                ExtProductChildren deserialized = this.mapper.readValue(file, typeref);
                trees.put(deserialized.getProductId(), deserialized);
            }
            catch (Exception e) {
                throw new RuntimeException("Could not read from file " + file + ": " + e.getMessage(), e);
            }
        }

        return trees;
    }



    public Map<String, ExtProduct> getProducts() {
        return this.getProductsImpl(null);
    }

    public Map<String, ExtProduct> getProducts(Collection<String> oids) {
        if (oids == null || oids.isEmpty()) {
            return Map.of();
        }

        return this.getProductsImpl(new HashSet<>(oids));
    }

    public Map<String, ExtProduct> getProducts(String... oids) {
        return oids != null ? this.getProducts(Arrays.asList(oids)) : null;
    }

    public ExtProduct getProduct(String oid) {
        Map<String, ExtProduct> result = this.getProducts(Arrays.asList(oid));
        return result != null && !result.isEmpty() ? result.get(0) : null;
    }

    public Map<String, ExtProductChildren> getProductChildren() {
        return this.getProductChildrenImpl(null);
    }

    public Map<String, ExtProductChildren> getProductChildren(Collection<String> oids) {
        if (oids == null || oids.isEmpty()) {
            return Map.of();
        }

        return this.getProductChildrenImpl(new HashSet<>(oids));
    }

    public Map<String, ExtProductChildren> getProductChildren(String... oids) {
        return oids != null ? this.getProductChildren(Arrays.asList(oids)) : null;
    }

    public ExtProductChildren getProductChildren(String oid) {
        Map<String, ExtProductChildren> result = this.getProductChildren(Arrays.asList(oid));
        return result != null && !result.isEmpty() ? result.get(0) : null;
    }

}
