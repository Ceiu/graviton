package com.redhat.graviton.db.model;

import java.time.Instant;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/*
    CREATE TABLE IF NOT EXISTS gv_contents (
      "id" VARCHAR(64) NOT NULL,
      "created" TIMESTAMP NOT NULL DEFAULT now(),
      "updated" TIMESTAMP NOT NULL DEFAULT now(),
      "oid" VARCHAR(64) NOT NULL UNIQUE,
      "name" VARCHAR(256) NOT NULL,
      "type" VARCHAR(32) NOT NULL,
      "enabled" BOOLEAN NOT NULL DEFAULT false,
      "label" VARCHAR(256),
      "arches" VARCHAR(256),
      "vendor" VARCHAR(256),
      "content_url" VARCHAR(256),
      "gpgUrl" VARCHAR(256),
      "required_tags" VARCHAR(256),
      "metadata" VARCHAR(256),
      "metadata_expiration" INTEGER,

      PRIMARY KEY ("id")
    );
    CREATE INDEX IF NOT EXISTS gv_contents_idx1 ON gv_contents ("oid");

    CREATE TABLE IF NOT EXISTS gv_content_required_products (
      "content_id" VARCHAR(64) NOT NULL,
      "product_oid" VARCHAR(256) NOT NULL,

      PRIMARY KEY ("content_id", "product_oid"),
      FOREIGN KEY ("content_id") REFERENCES gv_contents ("id") ON DELETE CASCADE
    );
*/

@Entity
@Table(name = Content.DB_TABLE)
public class Content extends TimestampedEntity<Content> {
    public static final String DB_TABLE = "gv_contents";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "oid", nullable = false)
    private String oid;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "label", nullable = true)
    private String label;

    @Column(name = "arches", nullable = true)
    private String arches;

    @Column(name = "vendor", nullable = true)
    private String vendor;

    @Column(name = "gpg_url", nullable = true)
    private String gpgUrl;

    @Column(name = "content_url", nullable = true)
    private String contentUrl;

    @Column(name = "required_tags", nullable = true)
    private String requiredTags;

    @Column(name = "release_version", nullable = true)
    private String releaseVersion;

    @Column(name = "metadata", nullable = true)
    private String metadata;

    @Column(name = "metadata_expiration", nullable = true)
    private int metadataExpiration;

    @ElementCollection
    @CollectionTable(name = "gv_content_required_products", joinColumns = @JoinColumn(name = "content_id"))
    @Column(name = "product_oid")
    private Set<String> requiredProductOids;


    public Content() {
        // intentionally left empty
    }


    // This is all generated; fix as appropriate

    public String getId() {
        return this.id;
    }

    public Content setId(String id) {
        this.id = id;
        return this;
    }

    public String getOid() {
        return this.oid;
    }

    public Content setOid(String oid) {
        this.oid = oid;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public Content setName(String name) {
        this.name = name;
        return this;
    }

    public String getType() {
        return this.type;
    }

    public Content setType(String type) {
        this.type = type;
        return this;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public Content setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public String getLabel() {
        return this.label;
    }

    public Content setLabel(String label) {
        this.label = label;
        return this;
    }

    public String getArches() {
        return this.arches;
    }

    public Content setArches(String arches) {
        this.arches = arches;
        return this;
    }

    public String getVendor() {
        return this.vendor;
    }

    public Content setVendor(String vendor) {
        this.vendor = vendor;
        return this;
    }

    public String getGpgUrl() {
        return this.gpgUrl;
    }

    public Content setGpgUrl(String gpgUrl) {
        this.gpgUrl = gpgUrl;
        return this;
    }

    public String getContentUrl() {
        return this.contentUrl;
    }

    public Content setContentUrl(String contentUrl) {
        this.contentUrl = contentUrl;
        return this;
    }

    public String getRequiredTags() {
        return this.requiredTags;
    }

    public Content setRequiredTags(String requiredTags) {
        this.requiredTags = requiredTags;
        return this;
    }

    public String getReleaseVersion() {
        return this.releaseVersion;
    }

    public Content setReleaseVersion(String releaseVersion) {
        this.releaseVersion = releaseVersion;
        return this;
    }

    public String getMetadata() {
        return this.metadata;
    }

    public Content setMetadata(String metadata) {
        this.metadata = metadata;
        return this;
    }

    public int getMetadataExpiration() {
        return this.metadataExpiration;
    }

    public Content setMetadataExpiration(int metadataExpiration) {
        this.metadataExpiration = metadataExpiration;
        return this;
    }

    public Set<String> getRequiredProductOids() {
        return this.requiredProductOids;
    }

    public Content setRequiredProductOids(Set<String> requiredProductOids) {
        this.requiredProductOids = requiredProductOids;
        return this;
    }

}
