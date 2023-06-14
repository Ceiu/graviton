package com.redhat.graviton.impl.datasource.fs.model;

import com.redhat.graviton.api.datasource.model.ExtSubscription;

import java.time.Instant;



public class FileSystemExtSubscription implements ExtSubscription {

    private String id;
    private String organizationId;
    private String type;
    private Instant startDate;
    private Instant endDate;
    private String productId;
    private String contractNumber;
    private String accountNumber;
    private String orderNumber;


    @Override
    public String getId() {
        return this.id;
    }

    public FileSystemExtSubscription setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public String getOrganizationId() {
        return this.organizationId;
    }

    public FileSystemExtSubscription setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
        return this;
    }

    @Override
    public String getType() {
        return this.type;
    }

    public FileSystemExtSubscription setType(String type) {
        this.type = type;
        return this;
    }

    @Override
    public Instant getStartDate() {
        return this.startDate;
    }

    public FileSystemExtSubscription setStartDate(Instant startDate) {
        this.startDate = startDate;
        return this;
    }

    @Override
    public Instant getEndDate() {
        return this.endDate;
    }

    public FileSystemExtSubscription setEndDate(Instant endDate) {
        this.endDate = endDate;
        return this;
    }

    @Override
    public String getProductId() {
        return this.productId;
    }

    public FileSystemExtSubscription setProductId(String productId) {
        this.productId = productId;
        return this;
    }

    @Override
    public String getContractNumber() {
        return this.contractNumber;
    }

    public FileSystemExtSubscription setContractNumber(String contractNumber) {
        this.contractNumber = contractNumber;
        return this;
    }

    @Override
    public String getAccountNumber() {
        return this.accountNumber;
    }

    public FileSystemExtSubscription setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
        return this;
    }

    @Override
    public String getOrderNumber() {
        return this.orderNumber;
    }

    public FileSystemExtSubscription setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
        return this;
    }

}
