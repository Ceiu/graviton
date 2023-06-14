package com.redhat.graviton.api.candlepin.cert;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class CertEntitlement {

    private String consumer;
    private Integer quantity;
    private CertSubscription subscription;
    private CertOrder order;
    private List<CertProduct> products;
    private CertPool pool;

    /**
     * @param uuid
     */
    public CertEntitlement setConsumer(String uuid) {
        this.consumer = uuid;
        return this;
    }

    /**
     * @param quantity
     */
    public CertEntitlement setQuantity(Integer quantity) {
        this.quantity = quantity;
        return this;
    }

    /**
     * @param subscription
     */
    public CertEntitlement setSubscription(CertSubscription subscription) {
        this.subscription = subscription;
        return this;
    }

    /**
     * @param order
     */
    public CertEntitlement setOrder(CertOrder order) {
        this.order = order;
        return this;
    }

    /**
     * @param products
     */
    public CertEntitlement setProducts(List<CertProduct> products) {
        this.products = products;
        return this;
    }

    public List<CertProduct> getProducts() {
        return this.products;
    }

    /**
     * @param pool the pool to set
     */
    public CertEntitlement setPool(CertPool pool) {
        this.pool = pool;
        return this;
    }


}
