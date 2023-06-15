package com.redhat.graviton.api.candlepin.model;



public class CPResourceDTO {

    private String rel;
    private String href;

    public CPResourceDTO() {
        // intentionally left empty
    }

    public String getRel() {
        return this.rel;
    }

    public CPResourceDTO setRel(String rel) {
        this.rel = rel;
        return this;
    }

    public String getHref() {
        return this.href;
    }

    public CPResourceDTO setHref(String href) {
        this.href = href;
        return this;
    }

}
