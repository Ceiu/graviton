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
    -- certificates (immutable)
    CREATE TABLE IF NOT EXISTS gv_certificates (
      "id" VARCHAR(64) NOT NULL,
      "created" TIMESTAMP NOT NULL DEFAULT now(),
      "serial" BIGINT NOT NULL,
      "valid_after" TIMESTAMP NOT NULL,
      "valid_until" TIMESTAMP NOT NULL,
      "certificate" TEXT NOT NULL,

      PRIMARY KEY ("id")
    );
    CREATE INDEX IF NOT EXISTS gv_certificates_idx1 ON gv_certificates ("serial");
*/

@Entity
@Table(name = Certificate.DB_TABLE)
public class Certificate {
    public static final String DB_TABLE = "gv_certificates";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "created", nullable = false)
    private Instant created;

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


    public Certificate() {
        // intentionally left empty
    }

    public String getId() {
        return this.id;
    }

    public Certificate setId(String id) {
        this.id = id;
        return this;
    }

    public Instant getCreated() {
        return this.created;
    }

    public Certificate setCreated(Instant created) {
        this.created = created;
        return this;
    }

    @PrePersist
    protected void onCreate() {
        if (this.getCreated() == null) {
            this.setCreated(Instant.now());
        }
    }

    public long getSerialNumber() {
        return this.serial != null ? this.serial : 0;
    }

    public Certificate setSerialNumber(long serial) {
        this.serial = serial;
        return this;
    }

    public Instant getValidAfter() {
        return this.validAfter;
    }

    public Certificate setValidAfter(Instant validAfter) {
        this.validAfter = validAfter;
        return this;
    }

    public Instant getValidUntil() {
        return this.validUntil;
    }

    public Certificate setValidUntil(Instant validUntil) {
        this.validUntil = validUntil;
        return this;
    }

    public String getPrivateKey() {
        return this.privateKey;
    }

    public Certificate setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
        return this;
    }

    public String getCertificate() {
        return this.certificate;
    }

    public Certificate setCertificate(String certificate) {
        this.certificate = certificate;
        return this;
    }

}
