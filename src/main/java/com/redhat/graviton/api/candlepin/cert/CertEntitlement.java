package com.redhat.graviton.api.candlepin.cert;

import java.util.List;



public class CertEntitlement {

    private String consumer;
    private Integer quantity;
    private CertSubscription subscription;
    private CertOrder order;
    private List<CertProduct> products;
    private CertPool pool;

    public CertEntitlement() {
        // empty
    }

    public String getConsumer() {
        return this.consumer;
    }

    public CertEntitlement setConsumer(String consumerUuid) {
        this.consumer = consumerUuid;
        return this;
    }

    public Integer getQuantity() {
        return this.quantity;
    }

    public CertEntitlement setQuantity(Integer quantity) {
        this.quantity = quantity;
        return this;
    }

    public CertSubscription getSubscription() {
        return this.subscription;
    }

    public CertEntitlement setSubscription(CertSubscription subscription) {
        this.subscription = subscription;
        return this;
    }

    public CertOrder getOrder() {
        return this.order;
    }

    public CertEntitlement setOrder(CertOrder order) {
        this.order = order;
        return this;
    }

    public List<CertProduct> getProducts() {
        return this.products;
    }

    public CertEntitlement setProducts(List<CertProduct> products) {
        this.products = products;
        return this;
    }

    public CertPool getPool() {
        return this.pool;
    }

    public CertEntitlement setPool(CertPool pool) {
        this.pool = pool;
        return this;
    }

}
