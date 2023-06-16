package com.redhat.graviton.api.candlepin.cert;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;



public class CertSubscription {

    private String sku;
    private String name;
    private Integer warning;
    private Integer sockets;
    private Integer ram;
    private Integer cores;
    private Boolean management;
    @JsonProperty("stacking_id")
    private String stackingId;
    @JsonProperty("virt_only")
    private Boolean virtOnly;
    private CertService service;
    private String usage;
    private List<String> roles;
    private List<String> addons;

    public CertSubscription() {
        // empty
    }

    public String getSku() {
        return this.sku;
    }

    public CertSubscription setSku(String sku) {
        this.sku = sku;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public CertSubscription setName(String name) {
        this.name = name;
        return this;
    }

    public Integer getWarning() {
        return this.warning;
    }

    public CertSubscription setWarning(Integer warning) {
        this.warning = warning;
        return this;
    }

    public Integer getSockets() {
        return this.sockets;
    }

    public CertSubscription setSockets(Integer sockets) {
        this.sockets = sockets;
        return this;
    }

    public Integer getRam() {
        return this.ram;
    }

    public CertSubscription setRam(Integer ram) {
        this.ram = ram;
        return this;
    }

    public Integer getCores() {
        return this.cores;
    }

    public CertSubscription setCores(Integer cores) {
        this.cores = cores;
        return this;
    }

    public Boolean getManagement() {
        return this.management;
    }

    public CertSubscription setManagement(Boolean management) {
        this.management = management;
        return this;
    }

    public String getStackingId() {
        return this.stackingId;
    }

    public CertSubscription setStackingId(String stackingId) {
        this.stackingId = stackingId;
        return this;
    }

    public Boolean getVirtOnly() {
        return this.virtOnly;
    }

    public CertSubscription setVirtOnly(Boolean virtOnly) {
        this.virtOnly = virtOnly;
        return this;
    }

    public CertService getService() {
        return this.service;
    }

    public CertSubscription setService(CertService service) {
        this.service = service;
        return this;
    }

    public String getUsage() {
        return this.usage;
    }

    public CertSubscription setUsage(String usage) {
        this.usage = usage;
        return this;
    }

    public List<String> getRoles() {
        return this.roles;
    }

    public CertSubscription setRoles(List<String> roles) {
        this.roles = roles;
        return this;
    }

    public List<String> getAddons() {
        return this.addons;
    }

    public CertSubscription setAddons(List<String> addons) {
        this.addons = addons;
        return this;
    }


}
