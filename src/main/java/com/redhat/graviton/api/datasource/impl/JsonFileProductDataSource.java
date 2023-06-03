package com.redhat.graviton.api.datasource.impl;

import com.redhat.graviton.api.datasource.ProductDataSource;
import com.redhat.graviton.api.datasource.model.*;

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
public class JsonFileProductDataSource implements ProductDataSource {
    private static final Logger LOG = Logger.getLogger(JsonFileProductDataSource.class);

    private static final String BASE_PATH = "/home/crog/devel/subscription_data/product_data";

    private final ObjectMapper mapper;

    @Inject
    public JsonFileProductDataSource(ObjectMapper mapper) {
        this.mapper = Objects.requireNonNull(mapper);
    }

    private Map<String, UpstreamProduct> getProductsImpl(Set<String> oidFilter) {

        File dir = new File(BASE_PATH);
        if (!dir.canRead() || !dir.isDirectory()) {
            throw new IllegalStateException("BASE_PATH is not readable or not a directory: " + BASE_PATH);
        }

        Map<String, UpstreamProduct> products = new HashMap<>();
        TypeReference<UpstreamProduct> typeref = new TypeReference<UpstreamProduct>() {};

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
                UpstreamProduct deserialized = this.mapper.readValue(file, typeref);
                products.put(deserialized.getId(), deserialized);
            }
            catch (Exception e) {
                throw new RuntimeException("Could not read from file " + file + ": " + e.getMessage(), e);
            }
        }

        return products;
    }

    private Map<String, UpstreamProductTree> getProductTreesImpl(Set<String> oidFilter) {

        File dir = new File(BASE_PATH);
        if (!dir.canRead() || !dir.isDirectory()) {
            throw new IllegalStateException("BASE_PATH is not readable or not a directory: " + BASE_PATH);
        }

        Map<String, UpstreamProductTree> trees = new HashMap<>();
        TypeReference<UpstreamProductTree> typeref = new TypeReference<UpstreamProductTree>() {};

        File[] jsonFiles = dir.listFiles((loc, name) -> {
            boolean match = name != null && name.endsWith("-tree.json");

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
                UpstreamProductTree deserialized = this.mapper.readValue(file, typeref);
                trees.put(deserialized.getOid(), deserialized);
            }
            catch (Exception e) {
                throw new RuntimeException("Could not read from file " + file + ": " + e.getMessage(), e);
            }
        }

        return trees;
    }



    public Map<String, UpstreamProduct> getProducts() {
        return this.getProductsImpl(null);
    }

    public Map<String, UpstreamProduct> getProducts(Collection<String> oids) {
        if (oids == null || oids.isEmpty()) {
            return Map.of();
        }

        return this.getProductsImpl(new HashSet<>(oids));
    }

    public Map<String, UpstreamProduct> getProducts(String... oids) {
        return oids != null ? this.getProducts(Arrays.asList(oids)) : null;
    }

    public UpstreamProduct getProduct(String oid) {
        Map<String, UpstreamProduct> result = this.getProducts(Arrays.asList(oid));
        return result != null && !result.isEmpty() ? result.get(0) : null;
    }

    public Map<String, UpstreamProductTree> getProductTrees() {
        return this.getProductTreesImpl(null);
    }

    public Map<String, UpstreamProductTree> getProductTrees(Collection<String> oids) {
        if (oids == null || oids.isEmpty()) {
            return Map.of();
        }

        return this.getProductTreesImpl(new HashSet<>(oids));
    }

    public Map<String, UpstreamProductTree> getProductTrees(String... oids) {
        return oids != null ? this.getProductTrees(Arrays.asList(oids)) : null;
    }

    public UpstreamProductTree getProductTree(String oid) {
        Map<String, UpstreamProductTree> result = this.getProductTrees(Arrays.asList(oid));
        return result != null && !result.isEmpty() ? result.get(0) : null;
    }

}
