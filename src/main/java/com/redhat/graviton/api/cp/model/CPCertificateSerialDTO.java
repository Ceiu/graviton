package com.redhat.graviton.api.cp.model;

import java.time.Instant;


public class CPCertificateSerialDTO {

    private Instant created;
    private Instant updated;
    private Long id;
    private Long serial;
    private Instant expiration;
    private Boolean revoked;

    public Long getId() {
        return this.id;
    }

    public CPCertificateSerialDTO setId(Long id) {
        this.id = id;
        return this;
    }

    public Instant getCreated() {
        return this.created;
    }

    public CPCertificateSerialDTO setCreated(Instant created) {
        this.created = created;
        return this;
    }

    public Instant getUpdated() {
        return this.updated;
    }

    public CPCertificateSerialDTO setUpdated(Instant updated) {
        this.updated = updated;
        return this;
    }

    public Long getSerial() {
        return this.serial;
    }

    public CPCertificateSerialDTO setSerial(Long serial) {
        this.serial = serial;
        return this;
    }

    public Instant getExpiration() {
        return this.expiration;
    }

    public CPCertificateSerialDTO setExpiration(Instant expiration) {
        this.expiration = expiration;
        return this;
    }

    public Boolean getRevoked() {
        return this.revoked;
    }

    public CPCertificateSerialDTO setRevoked(Boolean revoked) {
        this.revoked = revoked;
        return this;
    }

}
