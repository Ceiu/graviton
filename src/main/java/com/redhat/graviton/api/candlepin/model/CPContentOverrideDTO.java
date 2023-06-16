package com.redhat.graviton.api.candlepin.model;

import java.time.Instant;


public class CPContentOverrideDTO {

    private Instant created;
    private Instant updated;
    private String name;
    private String contentLabel;
    private String value;

    public CPContentOverrideDTO() {
        // empty
    }

    public Instant getCreated() {
        return this.created;
    }

    public CPContentOverrideDTO setCreated(Instant created) {
        this.created = created;
        return this;
    }

    public Instant getUpdated() {
        return this.updated;
    }

    public CPContentOverrideDTO setUpdated(Instant updated) {
        this.updated = updated;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public CPContentOverrideDTO setName(String name) {
        this.name = name;
        return this;
    }

    public String getContentLabel() {
        return this.contentLabel;
    }

    public CPContentOverrideDTO setContentLabel(String contentLabel) {
        this.contentLabel = contentLabel;
        return this;
    }

    public String getValue() {
        return this.value;
    }

    public CPContentOverrideDTO setValue(String value) {
        this.value = value;
        return this;
    }


}
