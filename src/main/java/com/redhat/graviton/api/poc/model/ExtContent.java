package com.redhat.graviton.api.poc.model;

import java.time.Instant;
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

public class ExtContent {

    private Instant created;
    private Instant updated;

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

    public ExtContent() {
        // empty
    }

    public Instant getCreated() {
        return this.created;
    }

    public ExtContent setCreated(Instant created) {
        this.created = created;
        return this;
    }

    public Instant getUpdated() {
        return this.updated;
    }

    public ExtContent setUpdated(Instant updated) {
        this.updated = updated;
        return this;
    }

    public String getId() {
        return this.id;
    }

    public ExtContent setId(String id) {
        this.id = id;
        return this;
    }

    public String getType() {
        return this.type;
    }

    public ExtContent setType(String type) {
        this.type = type;
        return this;
    }

    public String getLabel() {
        return this.label;
    }

    public ExtContent setLabel(String label) {
        this.label = label;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public ExtContent setName(String name) {
        this.name = name;
        return this;
    }

    public String getVendor() {
        return this.vendor;
    }

    public ExtContent setVendor(String vendor) {
        this.vendor = vendor;
        return this;
    }

    public String getContentUrl() {
        return this.contentUrl;
    }

    public ExtContent setContentUrl(String contentUrl) {
        this.contentUrl = contentUrl;
        return this;
    }

    public String getGpgUrl() {
        return this.gpgUrl;
    }

    public ExtContent setGpgUrl(String gpgUrl) {
        this.gpgUrl = gpgUrl;
        return this;
    }

    public String getRequiredTags() {
        return this.requiredTags;
    }

    public ExtContent setRequiredTags(String requiredTags) {
        this.requiredTags = requiredTags;
        return this;
    }

    public String getArches() {
        return this.arches;
    }

    public ExtContent setArches(String arches) {
        this.arches = arches;
        return this;
    }

    public String getReleaseVer() {
        return this.releaseVer;
    }

    public ExtContent setReleaseVer(String releaseVer) {
        this.releaseVer = releaseVer;
        return this;
    }

    public Integer getMetadataExpiration() {
        return this.metadataExpiration;
    }

    public ExtContent setMetadataExpiration(Integer metadataExpiration) {
        this.metadataExpiration = metadataExpiration;
        return this;
    }

    public Set<String> getRequiredProductIds() {
        return this.requiredProductIds;
    }

    public ExtContent setRequiredProductIds(Set<String> requiredProductIds) {
        this.requiredProductIds = requiredProductIds;
        return this;
    }








}
