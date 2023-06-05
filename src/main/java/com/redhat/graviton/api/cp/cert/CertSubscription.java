package com.redhat.graviton.api.cp.cert;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class CertSubscription {

    private String sku;
    private String name;
    private Integer warning;
    private Integer sockets;
    // RAM is specified in GB.
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

    /**
     * @param sku
     */
    public CertSubscription setSku(String sku) {
        this.sku = sku;
        return this;
    }

    /**
     * @param name
     */
    public CertSubscription setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * @param warning
     */
    public CertSubscription setWarning(Integer warning) {
        this.warning = warning;
        return this;
    }

    /**
     * @param sockets
     */
    public CertSubscription setSockets(Integer sockets) {
        this.sockets = sockets;
        return this;
    }

    /**
     * @param ram
     */
    public CertSubscription setRam(Integer ram) {
        this.ram = ram;
        return this;
    }

    /**
     * @param cores
     */
    public CertSubscription setCores(Integer cores) {
        this.cores = cores;
        return this;
    }

    /**
     * @param management
     */
    public CertSubscription setManagement(Boolean management) {
        this.management = management;
        return this;
    }

    /**
     * @param stackingId
     */
    public CertSubscription setStackingId(String stackingId) {
        this.stackingId = stackingId;
        return this;
    }

    /**
     * @param virtOnly
     */
    public CertSubscription setVirtOnly(Boolean virtOnly) {
        this.virtOnly = virtOnly;
        return this;
    }

    /**
     * @param service
     */
    public CertSubscription setService(CertService service) {
        this.service = service;
        return this;
    }

    public CertSubscription setRoles(List<String> roles) {
        this.roles = roles;
        return this;
    }

    public CertSubscription setUsage(String usage) {
        this.usage = usage;
        return this;
    }

    public CertSubscription setAddons(List<String> addons) {
        this.addons = addons;
        return this;
    }

}
