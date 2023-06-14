package com.redhat.graviton.certs;



public interface X509Extension {

    String getOid();

    boolean isCritical();

}
