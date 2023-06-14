package com.redhat.graviton.certs;



public class X509ByteExtension implements X509Extension {

    private String oid;
    private boolean critical;
    private byte[] value;

    public X509ByteExtension(String oid, boolean critical, byte[] value) {
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

    public byte[] getValue() {
        return this.value;
    }

}
