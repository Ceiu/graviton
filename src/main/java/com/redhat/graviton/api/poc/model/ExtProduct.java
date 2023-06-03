package com.redhat.graviton.api.poc.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


//     {
//     "created":null,
//     "updated":null,
//     "uuid":null,
//     "id":"DO180R",
//     "name":"Red Hat OpenShift I: Containers & Kubernetes Online Training",
//     "multiplier":1,
//     "attributes":[{"name":"product_family",
//         "value":"GLS"},
//         {"name":"expires_after",
//         "value":"90"},
//         {"name":"ph_product_line",
//         "value":"Online Learning"},
//         {"name":"roles",
//         "value":""},
//         {"name":"variant",
//         "value":"Role"},
//         {"name":"name",
//         "value":"Red Hat OpenShift I: Containers & Kubernetes Online Training"},
//         {"name":"description",
//         "value":"GLS"},
//         {"name":"ph_category",
//         "value":"GLS"},
//         {"name":"support_level",
//         "value":"None"},
//         {"name":"support_type",
//         "value":"None"},
//         {"name":"type",
//         "value":"MKT"},
//         {"name":"ph_product_name",
//         "value":"Role OpenShift"}],
//     "derivedProduct":null,
//     "providedProducts":[],
//     "dependentProductIds":[],
//     "branding":[],
//     "href":null,
//     "productContent":[]
//     }


public class ExtProduct {

    public static class ExtProductContent {
        private Boolean enabled;
        private ExtContent content;

        public ExtProductContent() {
            //empty
        }

        public Boolean getEnabled() {
            return this.enabled;
        }

        public ExtProductContent setEnabled(Boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public ExtContent getContent() {
            return this.content;
        }

        public ExtProductContent setContent(ExtContent content) {
            this.content = content;
            return this;
        }
    }

    private Instant created;
    private Instant updated;

    private String id;
    private String name;
    private Integer multiplier;

    private List<ExtAttribute> attributes;

    private ExtProduct derivedProduct;
    private List<ExtProduct> providedProducts;

    private Set<String> dependentProductIds;

    private List<ExtProductContent> productContent;


    public ExtProduct() {
        // empty
    }

    public Instant getCreated() {
        return this.created;
    }

    public ExtProduct setCreated(Instant created) {
        this.created = created;
        return this;
    }

    public Instant getUpdated() {
        return this.updated;
    }

    public ExtProduct setUpdated(Instant updated) {
        this.updated = updated;
        return this;
    }

    public String getId() {
        return this.id;
    }

    public ExtProduct setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public ExtProduct setName(String name) {
        this.name = name;
        return this;
    }

    public Integer getMultiplier() {
        return this.multiplier;
    }

    public ExtProduct setMultiplier(Integer multiplier) {
        this.multiplier = multiplier;
        return this;
    }

    public Map<String, String> getAttributesAsMap() {
        if (this.attributes == null) {
            return null;
        }

        return this.attributes.stream()
            .collect(Collectors.toMap(ExtAttribute::getName, ExtAttribute::getValue));
    }

    public List<ExtAttribute> getAttributes() {
        return this.attributes;
    }

    public ExtProduct setAttributes(List<ExtAttribute> attributes) {
        this.attributes = attributes;
        return this;
    }

    public ExtProduct getDerivedProduct() {
        return this.derivedProduct;
    }

    public ExtProduct setDerivedProduct(ExtProduct derivedProduct) {
        this.derivedProduct = derivedProduct;
        return this;
    }

    public List<ExtProduct> getProvidedProducts() {
        return this.providedProducts;
    }

    public ExtProduct setProvidedProducts(List<ExtProduct> providedProducts) {
        this.providedProducts = providedProducts;
        return this;
    }

    public Set<String> getDependentProductIds() {
        return this.dependentProductIds;
    }

    public ExtProduct setDependentProductIds(Set<String> dependentProductIds) {
        this.dependentProductIds = dependentProductIds;
        return this;
    }

    public List<ExtProductContent> getProductContent() {
        return this.productContent;
    }

    public ExtProduct setProductContent(List<ExtProductContent> productContent) {
        this.productContent = productContent;
        return this;
    }

}
