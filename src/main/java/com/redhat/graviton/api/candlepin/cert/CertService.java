package com.redhat.graviton.api.candlepin.cert;



public class CertService {

    private String level;
    private String type;

    public CertService() {
        // empty
    }

    public String getLevel() {
        return this.level;
    }

    public CertService setLevel(String level) {
        this.level = level;
        return this;
    }

    public String getType() {
        return this.type;
    }

    public CertService setType(String type) {
        this.type = type;
        return this;
    }


}
