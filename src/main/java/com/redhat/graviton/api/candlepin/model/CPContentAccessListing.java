package com.redhat.graviton.api.candlepin.model;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CPContentAccessListing {
    private Instant lastUpdate;
    private Map<Long, List<String>> content;

    public CPContentAccessListing() {
        this.content = new HashMap<>();
    }

    public CPContentAccessListing setLastUpdate(Instant lastUpdate) {
        this.lastUpdate = lastUpdate;
        return this;
    }

    public Instant getLastUpdate() {
        return this.lastUpdate;
    }

    public CPContentAccessListing setContentListing(Long serial, List<String> contentListing) {
        this.content.put(serial, contentListing);
        return this;
    }

    public Map<Long, List<String>> getContentListing() {
        return this.content;
    }
}
