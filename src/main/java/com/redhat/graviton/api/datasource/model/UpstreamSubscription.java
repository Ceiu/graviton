package com.redhat.graviton.api.datasource.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;



public class UpstreamSubscription {

    private String id;
    private String organizationId;
    private String type;
    private Instant startDate;
    private Instant endDate;
    private String productId;
    private String contractNumber;
    private String accountNumber;
    private String orderNumber;




    public String getId() {
        return this.id;
    }

    public UpstreamSubscription setId(String id) {
        this.id = id;
        return this;
    }

    public String getOrganizationId() {
        return this.organizationId;
    }

    public UpstreamSubscription setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
        return this;
    }

    public String getType() {
        return this.type;
    }

    public UpstreamSubscription setType(String type) {
        this.type = type;
        return this;
    }

    public Instant getStartDate() {
        return this.startDate;
    }

    public UpstreamSubscription setStartDate(Instant startDate) {
        this.startDate = startDate;
        return this;
    }

    public Instant getEndDate() {
        return this.endDate;
    }

    public UpstreamSubscription setEndDate(Instant endDate) {
        this.endDate = endDate;
        return this;
    }

    public String getProductId() {
        return this.productId;
    }

    public UpstreamSubscription setProductId(String productId) {
        this.productId = productId;
        return this;
    }

    public String getContractNumber() {
        return this.contractNumber;
    }

    public UpstreamSubscription setContractNumber(String contractNumber) {
        this.contractNumber = contractNumber;
        return this;
    }

    public String getAccountNumber() {
        return this.accountNumber;
    }

    public UpstreamSubscription setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
        return this;
    }

    public String getOrderNumber() {
        return this.orderNumber;
    }

    public UpstreamSubscription setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
        return this;
    }

}
