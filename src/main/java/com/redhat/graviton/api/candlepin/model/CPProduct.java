package com.redhat.graviton.api.candlepin.model;

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


public class CPProduct {

    public static class CPProductContent {
        private Boolean enabled;
        private CPContent content;

        public CPProductContent() {
            //empty
        }

        public Boolean getEnabled() {
            return this.enabled;
        }

        public CPProductContent setEnabled(Boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public CPContent getContent() {
            return this.content;
        }

        public CPProductContent setContent(CPContent content) {
            this.content = content;
            return this;
        }
    }

    private Instant created;
    private Instant updated;

    private String id;
    private String name;
    private Integer multiplier;

    private List<CPAttribute> attributes;

    private CPProduct derivedProduct;
    private List<CPProduct> providedProducts;

    private Set<String> dependentProductIds;

    private List<CPProductContent> productContent;


    public CPProduct() {
        // empty
    }

    public Instant getCreated() {
        return this.created;
    }

    public CPProduct setCreated(Instant created) {
        this.created = created;
        return this;
    }

    public Instant getUpdated() {
        return this.updated;
    }

    public CPProduct setUpdated(Instant updated) {
        this.updated = updated;
        return this;
    }

    public String getId() {
        return this.id;
    }

    public CPProduct setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public CPProduct setName(String name) {
        this.name = name;
        return this;
    }

    public Integer getMultiplier() {
        return this.multiplier;
    }

    public CPProduct setMultiplier(Integer multiplier) {
        this.multiplier = multiplier;
        return this;
    }

    public Map<String, String> getAttributesAsMap() {
        if (this.attributes == null) {
            return null;
        }

        return this.attributes.stream()
            .collect(Collectors.toMap(CPAttribute::getName, CPAttribute::getValue));
    }

    public List<CPAttribute> getAttributes() {
        return this.attributes;
    }

    public CPProduct setAttributes(List<CPAttribute> attributes) {
        this.attributes = attributes;
        return this;
    }

    public CPProduct getDerivedProduct() {
        return this.derivedProduct;
    }

    public CPProduct setDerivedProduct(CPProduct derivedProduct) {
        this.derivedProduct = derivedProduct;
        return this;
    }

    public List<CPProduct> getProvidedProducts() {
        return this.providedProducts;
    }

    public CPProduct setProvidedProducts(List<CPProduct> providedProducts) {
        this.providedProducts = providedProducts;
        return this;
    }

    public Set<String> getDependentProductIds() {
        return this.dependentProductIds;
    }

    public CPProduct setDependentProductIds(Set<String> dependentProductIds) {
        this.dependentProductIds = dependentProductIds;
        return this;
    }

    public List<CPProductContent> getProductContent() {
        return this.productContent;
    }

    public CPProduct setProductContent(List<CPProductContent> productContent) {
        this.productContent = productContent;
        return this;
    }

}
