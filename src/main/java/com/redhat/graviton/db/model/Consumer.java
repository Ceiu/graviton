package com.redhat.graviton.db.model;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;


/*
    -- consumers
    CREATE TABLE IF NOT EXISTS gv_consumers (
      "id" VARCHAR(64) NOT NULL,
      "created" TIMESTAMP NOT NULL DEFAULT now(),
      "updated" TIMESTAMP NOT NULL DEFAULT now(),
      "oid" VARCHAR(64) NOT NULL UNIQUE,
      "type" VARCHAR(32) NOT NULL,
      "name" VARCHAR(250) NOT NULL,
      "org_id" VARCHAR(64) NOT NULL,
      "username" VARCHAR(128),
      "last_check_in" TIMESTAMP NOT NULL DEFAULT now(),
      "last_cloud_profile_update" TIMESTAMP NOT NULL DEFAULT now(),
      "certificate_id" VARCHAR(64),
      "key_pair_id" VARCHAR(64),

      PRIMARY KEY ("id"),
      FOREIGN KEY ("org_id") REFERENCES gv_organizations ("id") ON DELETE CASCADE,
      FOREIGN KEY ("certificate_id") REFERENCES gv_certificates ("id"),
      FOREIGN KEY ("key_pair_id") REFERENCES gv_key_pairs ("id")
    );
    CREATE INDEX IF NOT EXISTS gv_consumers_idx1 ON gv_consumers ("oid");
    CREATE INDEX IF NOT EXISTS gv_consumers_idx2 ON gv_consumers ("org_id");
*/

@Entity
@Table(name = Consumer.DB_TABLE)
public class Consumer extends TimestampedEntity<Consumer> {

    public static final String DB_TABLE = "gv_consumers";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "oid", nullable = false)
    private String oid;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "type", nullable = false)
    private String type; // TODO: Convert to an enum

    @ManyToOne
    @JoinColumn(name = "org_id", nullable = false)
    private Organization organization;

    @Column(name = "username", nullable = true)
    private String username;

    @ElementCollection
    @CollectionTable(name = "gv_consumer_facts", joinColumns = @JoinColumn(name = "consumer_id"))
    @MapKeyColumn(name = "name")
    @Column(name = "value")
    private Map<String, String> facts;

    // installed products (I'm sure someone cares about this yet...)

    // hypervisor junk (hypervisor ID, reporter ID -- is that it? why is this a separate table in CP???)

    @Column(name = "last_check_in", nullable = true)
    private Instant lastCheckIn;

    @Column(name = "last_cloud_profile_update", nullable = true)
    private Instant lastCloudProfileUpdate;

    @ManyToOne
    @JoinColumn(name="key_pair_id", nullable = true)
    private KeyPairData keyPair;

    @ManyToOne
    @JoinColumn(name="certificate_id", nullable = true)
    private Certificate certificate;

    // What are these even for?
    // private Set<String> contentTags;

    // system purpose stuff (is this even important in SCA?)

    // @Column(name = "sp_role")
    // private String systemPurposeRole;

    // @Column(name = "sp_usage")
    // private String systemPurposeUsage;

    // @Column(name = "sp_status")
    // private String systemPurposeStatus;

    // @Column(name = "sp_service_type")
    // private String systemPurposeServiceType;

    // private Set<String> systemPurposeAddOns;



    public Consumer() {

    }

    public String getId() {
        return this.id;
    }

    public Consumer setId(String id) {
        this.id = id;
        return this;
    }

    public String getOid() {
        return this.oid;
    }

    public Consumer setOid(String oid) {
        this.oid = oid;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public Consumer setName(String name) {
        this.name = name;
        return this;
    }

    public String getType() {
        return this.type;
    }

    public Consumer setType(String type) {
        this.type = type;
        return this;
    }

    public Organization getOrganization() {
        return this.organization;
    }

    public Consumer setOrganization(Organization organization) {
        this.organization = organization;
        return this;
    }

    public String getUsername() {
        return this.username;
    }

    public Consumer setUsername(String username) {
        this.username = username;
        return this;
    }

    public Map<String, String> getFacts() {
        return this.facts;
    }

    public Consumer setFacts(Map<String, String> facts) {
        this.facts = facts;
        return this;
    }

    public Instant getLastCheckIn() {
        return this.lastCheckIn;
    }

    public Consumer setLastCheckIn(Instant lastCheckIn) {
        this.lastCheckIn = lastCheckIn;
        return this;
    }

    public Instant getLastCloudProfileUpdate() {
        return this.lastCloudProfileUpdate;
    }

    public Consumer setLastCloudProfileUpdate(Instant lastCloudProfileUpdate) {
        this.lastCloudProfileUpdate = lastCloudProfileUpdate;
        return this;
    }

    public KeyPairData getKeyPair() {
        return this.keyPair;
    }

    public Consumer setKeyPair(KeyPairData keyPair) {
        this.keyPair = keyPair;
        return this;
    }

    public Certificate getCertificate() {
        return this.certificate;
    }

    public Consumer setCertificate(Certificate certificate) {
        this.certificate = certificate;
        return this;
    }

}
