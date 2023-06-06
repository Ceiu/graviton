package com.redhat.graviton.api.cp.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;


public class CPConsumerDTO {

    private String id;
    private Instant created;
    private Instant updated;

    private String name;
    private String uuid;
    private CPConsumerTypeDTO type;

    private CPOwnerDTO owner;
    private String username;

    // private String serviceLevel; // system purpose stuff might be deprecated? follow up on this
    // private String serviceType;
    // private String role;
    // private String usage;
    // private String systemPurposeStatus;
    // private Set<String> addOns = null;
    // private ReleaseVerDTO releaseVer;

    // private EnvironmentDTO environment;
    // private Long entitlementCount;

    private Map<String, String> facts;
    private Instant lastCheckin;
    private List<CPInstalledProductDTO> installedProducts;

    // private Boolean canActivate; // what is this??
    // private Set<CapabilityDTO> capabilities = null;
    // private HypervisorIdDTO hypervisorId;
    // private Set<String> contentTags = null;
    // private Boolean autoheal;
    // private String annotations; // erroneous inclusion
    // private String contentAccessMode; // will always be SCA in graviton

    private CPCertificateDTO idCert;

    // private List<Object> guestIds = null;
    private String href;

    private List<Object> activationKeys;    // these should be empty lists, so we'll include them
    private List<Object> environments;      // without a defined object. Should be good enough for
                                            // serialization


    public String getId() {
        return this.id;
    }

    public CPConsumerDTO setId(String id) {
        this.id = id;
        return this;
    }

    public Instant getCreated() {
        return this.created;
    }

    public CPConsumerDTO setCreated(Instant created) {
        this.created = created;
        return this;
    }

    public Instant getUpdated() {
        return this.updated;
    }

    public CPConsumerDTO setUpdated(Instant updated) {
        this.updated = updated;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public CPConsumerDTO setName(String name) {
        this.name = name;
        return this;
    }

    public String getUuid() {
        return this.uuid;
    }

    public CPConsumerDTO setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public CPConsumerTypeDTO getType() {
        return this.type;
    }

    public CPConsumerDTO setType(CPConsumerTypeDTO type) {
        this.type = type;
        return this;
    }

    public CPOwnerDTO getOwner() {
        return this.owner;
    }

    public CPConsumerDTO setOwner(CPOwnerDTO owner) {
        this.owner = owner;
        return this;
    }

    public String getUsername() {
        return this.username;
    }

    public CPConsumerDTO setUsername(String username) {
        this.username = username;
        return this;
    }

    public Map<String, String> getFacts() {
        return this.facts;
    }

    public CPConsumerDTO setFacts(Map<String, String> facts) {
        this.facts = facts;
        return this;
    }

    public Instant getLastCheckin() {
        return this.lastCheckin;
    }

    public CPConsumerDTO setLastCheckin(Instant lastCheckin) {
        this.lastCheckin = lastCheckin;
        return this;
    }

    public List<CPInstalledProductDTO> getInstalledProducts() {
        return this.installedProducts;
    }

    public CPConsumerDTO setInstalledProducts(List<CPInstalledProductDTO> installedProducts) {
        this.installedProducts = installedProducts;
        return this;
    }

    public CPCertificateDTO getIdCert() {
        return this.idCert;
    }

    public CPConsumerDTO setIdCert(CPCertificateDTO idCert) {
        this.idCert = idCert;
        return this;
    }

    public String getHref() {
        return this.href;
    }

    public CPConsumerDTO setHref(String href) {
        this.href = href;
        return this;
    }

    public List<Object> getActivationKeys() {
        return this.activationKeys;
    }

    public CPConsumerDTO setActivationKeys(List<Object> activationKeys) {
        this.activationKeys = activationKeys;
        return this;
    }

    public List<Object> getEnvironments() {
        return this.environments;
    }

    public CPConsumerDTO setEnvironments(List<Object> environments) {
        this.environments = environments;
        return this;
    }

}
