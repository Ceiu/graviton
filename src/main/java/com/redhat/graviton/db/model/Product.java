package com.redhat.graviton.db.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.MapKey;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;


/*
    -- products
    CREATE TABLE IF NOT EXISTS gv_products (
      "id" VARCHAR(64) NOT NULL,
      "created" TIMESTAMP NOT NULL DEFAULT now(),
      "updated" TIMESTAMP NOT NULL DEFAULT now(),
      "oid" VARCHAR(64) NOT NULL UNIQUE,
      "name" VARCHAR(256) NOT NULL,
      "multiplier" INTEGER NOT NULL DEFAULT 1,

      PRIMARY KEY ("id")
    );
    CREATE INDEX IF NOT EXISTS gv_products_idx1 ON gv_products ("oid");

    -- product attributes
    CREATE TABLE IF NOT EXISTS gv_product_attributes (
      --may need an ID here for Hibernates sake
      "product_id" VARCHAR(64) NOT NULL,
      "name" VARCHAR(256) NOT NULL,
      "value" VARCHAR(256),

      PRIMARY KEY ("product_id", "name"),
      FOREIGN KEY ("product_id") REFERENCES gv_products ("id") ON DELETE CASCADE
    );
    CREATE INDEX IF NOT EXISTS gv_product_attrib_idx1 ON gv_product_attributes (product_id);

    -- dependent products
    CREATE TABLE IF NOT EXISTS gv_product_dependent_products (
      "product_id" VARCHAR(64) NOT NULL,
      "product_oid" VARCHAR(64) NOT NULL,

      PRIMARY KEY ("product_id", "product_oid"),
      FOREIGN KEY ("product_id") REFERENCES gv_products ("id") ON DELETE CASCADE
    );
    CREATE INDEX IF NOT EXISTS gv_product_dependent_products_idx1 ON gv_product_dependent_products ("product_id")

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

    -- product content
    CREATE TABLE IF NOT EXISTS gv_product_contents (
      "product_id" VARCHAR(64) NOT NULL,
      "content_id" VARCHAR(64) NOT NULL,

      PRIMARY KEY ("product_id", "content_id"),
      FOREIGN KEY ("product_id") REFERENCES gv_products ("id") ON DELETE CASCADE,
      FOREIGN KEY ("content_id") REFERENCES gv_contents ("id")
    );
    CREATE INDEX IF NOT EXISTS gv_product_contents_idx1 ON gv_product_contents ("product_id");

    -- product map (children products/product graph)
    CREATE TABLE IF NOT EXISTS gv_product_map (
      "product_id" VARCHAR(64) NOT NULL,
      "child_product_id" VARCHAR(64) NOT NULL,
      "tier" INTEGER NOT NULL DEFAULT 1
      "type" VARCHAR(32) NOT NULL,

      PRIMARY KEY ("product_id", "child_product_id"),
      FOREIGN KEY ("product_id") REFERENCES gv_products ("id") ON DELETE CASCADE,
      FOREIGN KEY ("child_product_id") REFERENCES gv_products ("id") ON DELETE CASCADE
    );
    CREATE INDEX IF NOT EXISTS gv_product_map_idx1 ON gv_product_map ("product_id");
    CREATE INDEX IF NOT EXISTS gv_product_map_idx2 ON gv_product_map ("child_product_id");
*/

@Entity
@Table(name = Product.DB_TABLE)
public class Product extends TimestampedEntity<Product> {
    public static final String DB_TABLE = "gv_products";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "oid", nullable = false)
    private String oid;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "multiplier", nullable = false)
    private int multiplier;

    @ElementCollection
    @CollectionTable(name = "gv_product_attributes", joinColumns = @JoinColumn(name = "product_id"))
    @MapKeyColumn(name = "name")
    @Column(name = "value")
    private Map<String, String> attributes;

    @ElementCollection
    @CollectionTable(name = "gv_product_dependent_products", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "product_oid")
    private Set<String> dependentProductOids;

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name = "product_id")
    private List<Branding> branding;

    // @OneToMany
    // @JoinTable(name = "gv_product_contents", joinColumns = @JoinColumn(name = "product_id"))
    // @MapKey(name = "oid")
    // private Map<String, Content> content;

    // @OneToMany
    // @JoinTable(name = "gv_product_map", joinColumns = @JoinColumn(name = "product_id"))
    // @WhereJoinTable(clause = "tier=1 AND type='provided'")
    // @MapKey(name = "oid")
    // private Map<String, Product> providedProducts;

    // @OneToMany
    // @JoinTable(name = "gv_product_map", joinColumns = @JoinColumn(name = "product_id"))
    // @WhereJoinTable(clause = "tier=1 AND type='derived'")
    // @MapKey(name = "oid")
    // private Map<String, Product> derivedProducts;


    public Product() {
        // intentionally left empty
    }

    public String getId() {
        return this.id;
    }

    public Product setId(String id) {
        this.id = id;
        return this;
    }

    public String getOid() {
        return this.oid;
    }

    public Product setOid(String oid) {
        this.oid = oid;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public Product setName(String name) {
        this.name = name;
        return this;
    }

    public int getMultiplier() {
        return this.multiplier;
    }

    public Product setMultiplier(int multiplier) {
        this.multiplier = multiplier;
        return this;
    }

    public Map<String, String> getAttributes() {
        return this.attributes;
    }

    public Product setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
        return this;
    }

    public Set<String> getDependentProductOids() {
        return this.dependentProductOids;
    }

    public Product setDependentProductOids(Set<String> dependentProductOids) {
        this.dependentProductOids = dependentProductOids;
        return this;
    }

    public List<Branding> getBranding() {
        return this.branding;
    }

    public Product setBranding(List<Branding> branding) {
        this.branding = branding;
        return this;
    }

    // public Map<String, Content> getContent() {
    //     return this.content;
    // }

    // public Product setContent(Map<String, Content> content) {
    //     this.content = content;
    //     return this;
    // }

    // public Map<String, Product> getDerivedProducts() {
    //     return this.derivedProducts;
    // }

    // public Product setDerivedProducts(Map<String, Product> derivedProducts) {
    //     this.derivedProducts = derivedProducts;
    //     return this;
    // }

    // public Map<String, Product> getProvidedProducts() {
    //     return this.providedProducts;
    // }

    // public Product setProvidedProducts(Map<String, Product> providedProducts) {
    //     this.providedProducts = providedProducts;
    //     return this;
    // }

}
