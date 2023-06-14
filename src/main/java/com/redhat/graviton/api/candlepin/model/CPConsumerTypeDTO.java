package com.redhat.graviton.api.candlepin.model;

import java.time.Instant;


public class CPConsumerTypeDTO {

    private Instant created;
    private Instant updated;
    private String id;
    private String label;
    private Boolean manifest;


    public Instant getCreated() {
        return this.created;
    }

    public CPConsumerTypeDTO setCreated(Instant created) {
        this.created = created;
        return this;
    }

    public Instant getUpdated() {
        return this.updated;
    }

    public CPConsumerTypeDTO setUpdated(Instant updated) {
        this.updated = updated;
        return this;
    }

    public String getId() {
        return this.id;
    }

    public CPConsumerTypeDTO setId(String id) {
        this.id = id;
        return this;
    }

    public String getLabel() {
        return this.label;
    }

    public CPConsumerTypeDTO setLabel(String label) {
        this.label = label;
        return this;
    }

    public Boolean getManifest() {
        return this.manifest;
    }

    public CPConsumerTypeDTO setManifest(Boolean manifest) {
        this.manifest = manifest;
        return this;
    }

}
