package com.redhat.graviton.api.candlepin.model;

import java.time.Instant;


public class CPCertificateDTO {

    private String id;
    private Instant created;
    private Instant updated;
    private String key;
    private String cert;
    private CPCertificateSerialDTO serial;


    public String getId() {
        return this.id;
    }

    public CPCertificateDTO setId(String id) {
        this.id = id;
        return this;
    }

    public Instant getCreated() {
        return this.created;
    }

    public CPCertificateDTO setCreated(Instant created) {
        this.created = created;
        return this;
    }

    public Instant getUpdated() {
        return this.updated;
    }

    public CPCertificateDTO setUpdated(Instant updated) {
        this.updated = updated;
        return this;
    }

    public String getKey() {
        return this.key;
    }

    public CPCertificateDTO setKey(String key) {
        this.key = key;
        return this;
    }

    public String getCert() {
        return this.cert;
    }

    public CPCertificateDTO setCert(String cert) {
        this.cert = cert;
        return this;
    }

    public CPCertificateSerialDTO getSerial() {
        return this.serial;
    }

    public CPCertificateDTO setSerial(CPCertificateSerialDTO serial) {
        this.serial = serial;
        return this;
    }

}
