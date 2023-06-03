package com.redhat.graviton.api.datasource.model;

import java.util.List;


public class UpstreamProductTree {

    private String oid;

    private List<String> derivedOids;
    private List<String> providedOids;


    public UpstreamProductTree() {
        // intentionally left empty
    }

    public String getOid() {  // this needs to be renamed to ID, probably
        return this.oid;
    }

    public UpstreamProductTree setOid(String oid) {
        this.oid = oid;
        return this;
    }

    public List<String> getDerivedOids() {
        return this.derivedOids;
    }

    public UpstreamProductTree setDerivedOids(List<String> derivedOids) {
        this.derivedOids = derivedOids;
        return this;
    }

    public List<String> getProvidedOids() {
        return this.providedOids;
    }

    public UpstreamProductTree setProvidedOids(List<String> providedOids) {
        this.providedOids = providedOids;
        return this;
    }


    public String toString() {
        return String.format("UpstreamProductTree [oid: %s, derived: %s, provided: %s]", this.getOid(),
            this.getDerivedOids(), this.getProvidedOids());
    }

}
