package com.redhat.graviton.api.candlepin.cert;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;


public class CertService {

    private String level;
    private String type;

    /**
     * @param level
     */
    public CertService setLevel(String level) {
        this.level = level;
        return this;
    }

    /**
     * @param type
     */
    public CertService setType(String type) {
        this.type = type;
        return this;
    }

}
