package com.redhat.graviton.api.candlepin.cert;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;


public class CertContent {

    private String id;
    private String type;
    private String name;
    private String label;
    private String vendor;
    private String path;
    @JsonProperty("gpg_url")
    private String gpgUrl;
    private Boolean enabled;
    @JsonProperty("metadata_expire")
    private Integer metadataExpiration;
    @JsonProperty("required_tags")
    private List<String> requiredTags;
    private List<String> arches;

    public CertContent() {
        // empty
    }

    public String getId() {
        return this.id;
    }

    public CertContent setId(String id) {
        this.id = id;
        return this;
    }

    public String getType() {
        return this.type;
    }

    public CertContent setType(String type) {
        this.type = type;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public CertContent setName(String name) {
        this.name = name;
        return this;
    }

    public String getLabel() {
        return this.label;
    }

    public CertContent setLabel(String label) {
        this.label = label;
        return this;
    }

    public String getVendor() {
        return this.vendor;
    }

    public CertContent setVendor(String vendor) {
        this.vendor = vendor;
        return this;
    }

    public String getPath() {
        return this.path;
    }

    public CertContent setPath(String path) {
        this.path = path;
        return this;
    }

    public String getGpgUrl() {
        return this.gpgUrl;
    }

    public CertContent setGpgUrl(String gpgUrl) {
        this.gpgUrl = gpgUrl;
        return this;
    }

    public Boolean getEnabled() {
        return this.enabled;
    }

    public CertContent setEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public Integer getMetadataExpiration() {
        return this.metadataExpiration;
    }

    public CertContent setMetadataExpiration(Integer metadataExpiration) {
        this.metadataExpiration = metadataExpiration;
        return this;
    }

    public List<String> getRequiredTags() {
        return this.requiredTags;
    }

    public CertContent setRequiredTags(List<String> requiredTags) {
        this.requiredTags = requiredTags;
        return this;
    }

    public List<String> getArches() {
        return this.arches;
    }

    public CertContent setArches(List<String> arches) {
        this.arches = arches;
        return this;
    }

}
