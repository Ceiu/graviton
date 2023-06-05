package com.redhat.graviton.api.cp.cert;

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
    private Integer metadataExpire;
    @JsonProperty("required_tags")
    private List<String> requiredTags;
    private List<String> arches;

    /**
     * @param id
     */
    public CertContent setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * @param type
     */
    public CertContent setType(String type) {
        this.type = type;
        return this;
    }

    /**
     * @param name
     */
    public CertContent setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * @param label
     */
    public CertContent setLabel(String label) {
        this.label = label;
        return this;
    }

    /**
     * @param vendor
     */
    public CertContent setVendor(String vendor) {
        this.vendor = vendor;
        return this;
    }

    /**
     * @param path
     */
    public CertContent setPath(String path) {
        this.path = path;
        return this;
    }

    /**
     *@return path
     */
    public String getPath() {
        return this.path;
    }

    /**
     * @param gpgUrl
     */
    public CertContent setGpgUrl(String gpgUrl) {
        this.gpgUrl = gpgUrl;
        return this;
    }

    /**
     * @param enabled
     */
    public CertContent setEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * @param metadataExpire
     */
    public CertContent setMetadataExpiration(Integer metadataExpire) {
        this.metadataExpire = metadataExpire;
        return this;
    }

    /**
     * @param requiredTags
     */
    public CertContent setRequiredTags(List<String> requiredTags) {
        this.requiredTags = requiredTags;
        return this;
    }

    /**
     * @return the arches
     */
    public List<String> getArches() {
        return arches;
    }

    /**
     * @param arches the arches to set
     */
    public CertContent setArches(List<String> arches) {
        this.arches = arches;
        return this;
    }
}
