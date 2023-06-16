package com.redhat.graviton.api.candlepin.cert;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;



public class CertProduct {

    private String id;
    private String name;
    private String version;
    @JsonProperty("brand_type")
    private String brandType;
    @JsonProperty("brand_name")
    private String brandName;
    private List<String> architectures;
    private List<CertContent> content;


    public CertProduct() {
        // empty
    }

    public String getId() {
        return this.id;
    }

    public CertProduct setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public CertProduct setName(String name) {
        this.name = name;
        return this;
    }

    public String getVersion() {
        return this.version;
    }

    public CertProduct setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getBrandType() {
        return this.brandType;
    }

    public CertProduct setBrandType(String brandType) {
        this.brandType = brandType;
        return this;
    }

    public String getBrandName() {
        return this.brandName;
    }

    public CertProduct setBrandName(String brandName) {
        this.brandName = brandName;
        return this;
    }

    public List<String> getArchitectures() {
        return this.architectures;
    }

    public CertProduct setArchitectures(List<String> architectures) {
        this.architectures = architectures;
        return this;
    }

    public List<CertContent> getContent() {
        return this.content;
    }

    public CertProduct setContent(List<CertContent> content) {
        this.content = content;
        return this;
    }



}
