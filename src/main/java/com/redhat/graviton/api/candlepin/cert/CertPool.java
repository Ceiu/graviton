package com.redhat.graviton.api.candlepin.cert;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class CertPool {

    private String id;

    public CertPool setId(String id) {
        this.id = id;
        return this;
    }

}
