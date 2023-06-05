package com.redhat.graviton.api.cp.cert;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class CertOrder {

    private String number;
    private Long quantity;
    private String start;
    private String end;
    private String contract;
    private String account;

    /**
     * @param number
     */
    public CertOrder setNumber(String number) {
        this.number = number;
        return this;
    }

    /**
     * @param quantity
     */
    public CertOrder setQuantity(Long quantity) {
        this.quantity = quantity;
        return this;
    }

    /**
     * @param start
     */
    public CertOrder setStart(String start) {
        this.start = start;
        return this;
    }

    /**
     * @param end
     */
    public CertOrder setEnd(String end) {
        this.end = end;
        return this;
    }

    /**
     * @param contract
     */
    public CertOrder setContract(String contract) {
        this.contract = contract;
        return this;
    }

    /**
     * @param account
     */
    public CertOrder setAccount(String account) {
        this.account = account;
        return this;
    }

}
