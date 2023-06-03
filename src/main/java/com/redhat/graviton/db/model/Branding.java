package com.redhat.graviton.db.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;



/*
    -- product branding
    CREATE TABLE IF NOT EXISTS gv_product_branding (
      "id" VARCHAR(64) NOT NULL,
      "product_id" VARCHAR(64) NOT NULL,
      "product_oid" VARCHAR(64) NOT NULL,
      "name" VARCHAR(256),
      "type" VARCHAR(256),

      PRIMARY KEY ("id"),
      FOREIGN KEY ("product_id") REFERENCES gv_products ("id") ON DELETE CASCADE
    );
    CREATE INDEX IF NOT EXISTS gv_product_branding_idx1 ON gv_product_branding (product_id);
*/

@Entity
@Table(name = Branding.DB_TABLE)
public class Branding {
    public static final String DB_TABLE = "gv_product_branding";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "product_oid", nullable = false)
    private String productOid;

    @Column(name = "name", nullable = true)
    private String name;

    @Column(name = "type", nullable = true)
    private String type;

    public Branding() {
        // intentionally left empty
    }

    public String getId() {
        return this.id;
    }

    public Branding setId(String id) {
        this.id = id;
        return this;
    }

    public String getProductOid() {
        return this.productOid;
    }

    public Branding setProductOid(String productOid) {
        this.productOid = productOid;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public Branding setName(String name) {
        this.name = name;
        return this;
    }

    public String getType() {
        return this.type;
    }

    public Branding setType(String type) {
        this.type = type;
        return this;
    }

}
