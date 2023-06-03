package com.redhat.graviton.api.datasource.model;

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


public class UpstreamProduct {

    // public static class UpstreamProductContent {
    //     private Boolean enabled;
    //     private ExtContent content;

    //     public UpstreamProductContent() {
    //         //empty
    //     }

    //     public Boolean getEnabled() {
    //         return this.enabled;
    //     }

    //     public UpstreamProductContent setEnabled(Boolean enabled) {
    //         this.enabled = enabled;
    //         return this;
    //     }

    //     public ExtContent getContent() {
    //         return this.content;
    //     }

    //     public UpstreamProductContent setContent(ExtContent content) {
    //         this.content = content;
    //         return this;
    //     }
    // }

    private Instant created;
    private Instant updated;

    private String id;
    private String name;
    private Integer multiplier;

    private Map<String, String> attributes;
    private Set<String> dependentProductIds;

    private List<UpstreamContent> content;

    // private UpstreamProduct derivedProduct;
    // private List<UpstreamProduct> providedProducts;

    public UpstreamProduct() {
        // empty
    }

    public Instant getCreated() {
        return this.created;
    }

    public UpstreamProduct setCreated(Instant created) {
        this.created = created;
        return this;
    }

    public Instant getUpdated() {
        return this.updated;
    }

    public UpstreamProduct setUpdated(Instant updated) {
        this.updated = updated;
        return this;
    }

    public String getId() {
        return this.id;
    }

    public UpstreamProduct setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public UpstreamProduct setName(String name) {
        this.name = name;
        return this;
    }

    public Integer getMultiplier() {
        return this.multiplier;
    }

    public UpstreamProduct setMultiplier(Integer multiplier) {
        this.multiplier = multiplier;
        return this;
    }

    public Map<String, String> getAttributes() {
        return this.attributes;
    }

    public UpstreamProduct setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
        return this;
    }

    public Set<String> getDependentProductIds() {
        return this.dependentProductIds;
    }

    public UpstreamProduct setDependentProductIds(Set<String> dependentProductIds) {
        this.dependentProductIds = dependentProductIds;
        return this;
    }

    public List<UpstreamContent> getContent() {
        return this.content;
    }

    public UpstreamProduct setContent(List<UpstreamContent> content) {
        this.content = content;
        return this;
    }

    public String toString() {
        List<UpstreamContent> content = this.getContent();
        int contentCount = content != null ? content.size() : 0;

        return String.format("UpstreamProduct [id: %s, name: %s, content: %d]", this.getId(), this.getName(),
            contentCount);
    }


}
