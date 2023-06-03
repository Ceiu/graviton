package com.redhat.graviton.api.cp.model;



public class CPConsumerDTO {

    private String value;

    public CPConsumerDTO setValue(String value) {
        this.value = value;
        return this;
    }

    public String getValue() {
        return this.value;
    }

}
