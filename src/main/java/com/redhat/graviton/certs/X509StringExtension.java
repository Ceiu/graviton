package com.redhat.graviton.certs;



public class X509StringExtension implements X509Extension {

    private String oid;
    private boolean critical;
    private String value;

    public X509StringExtension(String oid, boolean critical, String value) {
        if (oid == null || oid.isEmpty()) {
            throw new IllegalArgumentException("oid is null or empty");
        }

        this.oid = oid;
        this.critical = critical;
        this.value = value;
    }

    public String getOid() {
        return this.oid;
    }

    public boolean isCritical() {
        return this.critical;
    }

    public String getValue() {
        return this.value;
    }

}
