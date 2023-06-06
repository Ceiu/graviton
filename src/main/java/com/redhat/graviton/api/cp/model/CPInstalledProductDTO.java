package com.redhat.graviton.api.cp.model;

import java.time.Instant;


public class CPInstalledProductDTO {

    private Instant created;
    private Instant updated;
    private String id;
    private String productId;
    private String productName;
    private String version;
    private String arch;
    private String status;
    private Instant startDate;
    private Instant endDate;

    public CPInstalledProductDTO() {

    }


    public Instant getCreated() {
        return this.created;
    }

    public CPInstalledProductDTO setCreated(Instant created) {
        this.created = created;
        return this;
    }

    public Instant getUpdated() {
        return this.updated;
    }

    public CPInstalledProductDTO setUpdated(Instant updated) {
        this.updated = updated;
        return this;
    }

    public String getId() {
        return this.id;
    }

    public CPInstalledProductDTO setId(String id) {
        this.id = id;
        return this;
    }

    public String getProductId() {
        return this.productId;
    }

    public CPInstalledProductDTO setProductId(String productId) {
        this.productId = productId;
        return this;
    }

    public String getProductName() {
        return this.productName;
    }

    public CPInstalledProductDTO setProductName(String productName) {
        this.productName = productName;
        return this;
    }

    public String getVersion() {
        return this.version;
    }

    public CPInstalledProductDTO setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getArch() {
        return this.arch;
    }

    public CPInstalledProductDTO setArch(String arch) {
        this.arch = arch;
        return this;
    }

    public String getStatus() {
        return this.status;
    }

    public CPInstalledProductDTO setStatus(String status) {
        this.status = status;
        return this;
    }

    public Instant getStartDate() {
        return this.startDate;
    }

    public CPInstalledProductDTO setStartDate(Instant startDate) {
        this.startDate = startDate;
        return this;
    }

    public Instant getEndDate() {
        return this.endDate;
    }

    public CPInstalledProductDTO setEndDate(Instant endDate) {
        this.endDate = endDate;
        return this;
    }

}
