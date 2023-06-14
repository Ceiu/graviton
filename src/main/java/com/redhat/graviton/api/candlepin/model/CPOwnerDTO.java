package com.redhat.graviton.api.candlepin.model;

import java.time.Instant;


public class CPOwnerDTO {

    private String id;
    private String key;
    private String displayName;
    private String href;
    private String contentAccessMode;

    public String getId() {
        return this.id;
    }

    public CPOwnerDTO setId(String id) {
        this.id = id;
        return this;
    }

    public String getKey() {
        return this.key;
    }

    public CPOwnerDTO setKey(String key) {
        this.key = key;
        return this;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public CPOwnerDTO setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public String getHref() {
        return this.href;
    }

    public CPOwnerDTO setHref(String href) {
        this.href = href;
        return this;
    }

    public String getContentAccessMode() {
        return this.contentAccessMode;
    }

    public CPOwnerDTO setContentAccessMode(String contentAccessMode) {
        this.contentAccessMode = contentAccessMode;
        return this;
    }


}
