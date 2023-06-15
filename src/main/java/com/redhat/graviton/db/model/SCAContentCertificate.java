package com.redhat.graviton.db.model;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;



/*
    -- sca cert junk
    CREATE TABLE IF NOT EXISTS gv_sca_content_certs (
      "id" VARCHAR(64) NOT NULL,
      "created" TIMESTAMP NOT NULL DEFAULT now(),
      "updated" TIMESTAMP NOT NULL DEFAULT now(),
      "filter" VARCHAR(255) NOT NULL UNIQUE,
      "serial" BIGINT NOT NULL,
      "valid_after" TIMESTAMP NOT NULL,
      "valid_until" TIMESTAMP NOT NULL,
      "private_key" TEXT NOT NULL,
      "certificate" TEXT NOT NULL,
      "content_data" TEXT NOT NULL,
      "data_signature" TEXT NOT NULL,

      PRIMARY KEY ("id")
    )
    CREATE INDEX IF NOT EXISTS gv_sca_content_certs_idx1 ON gv_sca_content_certs ("serial");
    CREATE INDEX IF NOT EXISTS gv_sca_content_certs_idx2 ON gv_sca_content_certs ("filter");
*/

// THIS IS NOT INTENDED TO BE IMPLEMENTED THIS WAY IN A REAL IMPLEMENTATION. IT EXISTS ONLY FOR
// SUBMAN COMPATIBILITY IN THE DEMO. STORING THE DATA THIS WAY IS PAAAAAAAAAAAAAAAIN

@Entity
@Table(name = SCAContentCertificate.DB_TABLE)
public class SCAContentCertificate extends TimestampedEntity {
    public static final String DB_TABLE = "gv_sca_content_certs";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "filter", nullable = false)
    private String filter;

    @Column(name = "serial", nullable = false)
    private Long serial;

    @Column(name = "valid_after", nullable = false)
    private Instant validAfter;

    @Column(name = "valid_until", nullable = false)
    private Instant validUntil;

    @Column(name = "private_key", nullable = false)
    private String privateKey;

    @Column(name = "certificate", nullable = false)
    private String certificate;

    @Column(name = "content_data", nullable = false)
    private String contentData;




    public SCAContentCertificate() {
        // intentionally left empty
    }

    public String getId() {
        return this.id;
    }

    public SCAContentCertificate setId(String id) {
        this.id = id;
        return this;
    }

    public String getFilter() {
        return this.filter;
    }

    public SCAContentCertificate setFilter(String filter) {
        this.filter = filter;
        return this;
    }

    public long getSerialNumber() {
        return this.serial != null ? this.serial : 0;
    }

    public SCAContentCertificate setSerialNumber(long serial) {
        this.serial = serial;
        return this;
    }

    public Instant getValidAfter() {
        return this.validAfter;
    }

    public SCAContentCertificate setValidAfter(Instant validAfter) {
        this.validAfter = validAfter;
        return this;
    }

    public Instant getValidUntil() {
        return this.validUntil;
    }

    public SCAContentCertificate setValidUntil(Instant validUntil) {
        this.validUntil = validUntil;
        return this;
    }

    public String getPrivateKey() {
        return this.privateKey;
    }

    public SCAContentCertificate setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
        return this;
    }

    public String getCertificate() {
        return this.certificate;
    }

    public SCAContentCertificate setCertificate(String certificate) {
        this.certificate = certificate;
        return this;
    }

    public String getContentData() {
        return this.contentData;
    }

    public SCAContentCertificate setContentData(String contentData) {
        this.contentData = contentData;
        return this;
    }

}
