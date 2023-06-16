package com.redhat.graviton.api.candlepin.cert;



public class CertOrder {

    private String number;
    private Long quantity;
    private String start;
    private String end;
    private String contract;
    private String account;

    public CertOrder() {
        // empty
    }


    public String getNumber() {
        return this.number;
    }

    public CertOrder setNumber(String number) {
        this.number = number;
        return this;
    }

    public Long getQuantity() {
        return this.quantity;
    }

    public CertOrder setQuantity(Long quantity) {
        this.quantity = quantity;
        return this;
    }

    public String getStart() {
        return this.start;
    }

    public CertOrder setStart(String start) {
        this.start = start;
        return this;
    }

    public String getEnd() {
        return this.end;
    }

    public CertOrder setEnd(String end) {
        this.end = end;
        return this;
    }

    public String getContract() {
        return this.contract;
    }

    public CertOrder setContract(String contract) {
        this.contract = contract;
        return this;
    }

    public String getAccount() {
        return this.account;
    }

    public CertOrder setAccount(String account) {
        this.account = account;
        return this;
    }

}
