package com.redhat.graviton.db.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;



/*
    CREATE TABLE IF NOT EXISTS gv_organizations (
      "id" VARCHAR(64) NOT NULL,
      "created" TIMESTAMP NOT NULL DEFAULT now(),
      "updated" TIMESTAMP,
      "oid" VARCHAR(64) NOT NULL UNIQUE, -- "CP account, org_key, or upstream org ID"
      "name" VARCHAR(256) NOT NULL,
      "parent_org_id" VARCHAR(64),

      PRIMARY KEY ("id"),
      FOREIGN KEY ("parent_org_id") REFERENCES gv_organizations ("id")
    );
    CREATE INDEX IF NOT EXISTS gv_organizations_idx1 ON gv_organizations ("oid");
*/

@Entity
@Table(name = Organization.DB_TABLE)
public class Organization {
    public static final String DB_TABLE = "gv_organizations";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "created", nullable = false)
    private Instant created;

    @Column(name = "updated", nullable = true)
    private Instant updated;

    @Column(name = "oid", nullable = false)
    private String oid;

    @Column(name = "name", nullable = false)
    private String name;

    // @ManyToOne
    // @JoinColumn(name = "parent_org_id", nullable = true)
    // private Organization parent;


    public Organization() {

    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();

        if (this.getCreated() == null) {
            this.setCreated(now);
        }

        this.setUpdated(now);
    }

    @PreUpdate
    protected void onUpdate() {
        this.setUpdated(Instant.now());
    }



    public String getId() {
        return this.id;
    }

    public Organization setId(String id) {
        this.id = id;
        return this;
    }

    public Instant getCreated() {
        return this.created;
    }

    public Organization setCreated(Instant created) {
        this.created = created;
        return this;
    }

    public Instant getUpdated() {
        return this.updated;
    }

    public Organization setUpdated(Instant updated) {
        this.updated = updated;
        return this;
    }

    public String getOid() {
        return this.oid;
    }

    public Organization setOid(String oid) {
        this.oid = oid;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public Organization setName(String name) {
        this.name = name;
        return this;
    }

    // public Organization getParent() {
    //     return this.parent;
    // }

    // public Organization setParent(Organization parent) {
    //     this.parent = parent;
    //     return this;
    // }

}
