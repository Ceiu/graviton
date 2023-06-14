package com.redhat.graviton.impl.datasource.fs.model;

import com.redhat.graviton.api.datasource.model.ExtContent;

import java.util.Set;


/*
{
    "created":null,
    "updated":null,
    "uuid":null,
    "id":"7165",
    "type":"yum",
    "label":"rhel-5-server-mrg-messaging-base-1-rpms",
    "name":"Red Hat Enterprise MRG Messaging Base v1 (for RHEL 5 Server) (RPMs)",
    "vendor":"Red Hat",
    "contentUrl":"/content/dist/rhel/server/5/$releasever/$basearch/mrg-m-base/1/os",
    "requiredTags":"rhel-5-server",
    "gpgUrl":"file:///etc/pki/rpm-gpg/RPM-GPG-KEY-redhat-release",
    "modifiedProductIds":[],
    "arches":"x86,
    x86_64",
    "metadataExpiration":86400,
    "requiredProductIds":[],
    "releaseVer":null
},
*/

public class FileSystemExtContent implements ExtContent {

    private String id;
    private String type;
    private String label;
    private String name;
    private String vendor;
    private String contentUrl;
    private String gpgUrl;
    private String requiredTags;
    private String arches;
    private String releaseVer;
    private Integer metadataExpiration;
    private Set<String> requiredProductIds;
    private Boolean enabled;

    public FileSystemExtContent() {
        // empty
    }

    @Override
    public String getId() {
        return this.id;
    }

    public FileSystemExtContent setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public String getType() {
        return this.type;
    }

    public FileSystemExtContent setType(String type) {
        this.type = type;
        return this;
    }

    @Override
    public String getLabel() {
        return this.label;
    }

    public FileSystemExtContent setLabel(String label) {
        this.label = label;
        return this;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public FileSystemExtContent setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String getVendor() {
        return this.vendor;
    }

    public FileSystemExtContent setVendor(String vendor) {
        this.vendor = vendor;
        return this;
    }

    @Override
    public String getContentUrl() {
        return this.contentUrl;
    }

    public FileSystemExtContent setContentUrl(String contentUrl) {
        this.contentUrl = contentUrl;
        return this;
    }

    @Override
    public String getGpgUrl() {
        return this.gpgUrl;
    }

    public FileSystemExtContent setGpgUrl(String gpgUrl) {
        this.gpgUrl = gpgUrl;
        return this;
    }

    @Override
    public String getRequiredTags() {
        return this.requiredTags;
    }

    public FileSystemExtContent setRequiredTags(String requiredTags) {
        this.requiredTags = requiredTags;
        return this;
    }

    @Override
    public String getArches() {
        return this.arches;
    }

    public FileSystemExtContent setArches(String arches) {
        this.arches = arches;
        return this;
    }

    @Override
    public String getReleaseVer() {
        return this.releaseVer;
    }

    public FileSystemExtContent setReleaseVer(String releaseVer) {
        this.releaseVer = releaseVer;
        return this;
    }

    @Override
    public Integer getMetadataExpiration() {
        return this.metadataExpiration;
    }

    public FileSystemExtContent setMetadataExpiration(Integer metadataExpiration) {
        this.metadataExpiration = metadataExpiration;
        return this;
    }

    @Override
    public Set<String> getRequiredProductIds() {
        return this.requiredProductIds;
    }

    public FileSystemExtContent setRequiredProductIds(Set<String> requiredProductIds) {
        this.requiredProductIds = requiredProductIds;
        return this;
    }

    @Override
    public Boolean isEnabled() {
        return this.enabled;
    }

    public FileSystemExtContent setEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

}
