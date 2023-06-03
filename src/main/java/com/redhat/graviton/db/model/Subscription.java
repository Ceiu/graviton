package com.redhat.graviton.db.model;

import java.time.Instant;
import java.util.Map;
import java.util.List;

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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/*
    -- subscriptions
    CREATE TABLE IF NOT EXISTS gv_subscriptions (
      "id" VARCHAR(64) NOT NULL,
      "created" TIMESTAMP NOT NULL DEFAULT now(),
      "updated" TIMESTAMP NOT NULL DEFAULT now(),
      "oid" VARCHAR(64) NOT NULL, -- are subscription OIDs globally unique?
      "org_id" VARCHAR(64) NOT NULL,
      "type" VARCHAR(32) NOT NULL,
      "product_id" VARCHAR(64) NOT NULL,
      "start_date" TIMESTAMP NOT NULL,
      "end_date" TIMESTAMP NOT NULL,
      "contract_number" VARCHAR(256),
      "account_number" VARCHAR(256),
      "order_number" VARCHAR(256),

      PRIMARY KEY ("id"),
      FOREIGN KEY ("org_id") REFERENCES gv_organizations ("id") ON DELETE CASCADE,
      FOREIGN KEY ("product_id") REFERENCES gv_products ("id")
    );
    CREATE INDEX IF NOT EXISTS gv_subs_idx1 ON gv_subscriptions ("oid");
    CREATE INDEX IF NOT EXISTS gv_subs_idx2 ON gv_subscriptions ("org_id");
    CREATE INDEX IF NOT EXISTS gv_subs_idx3 ON gv_subscriptions ("product_id");
    CREATE INDEX IF NOT EXISTS gv_subs_idx4 ON gv_subscriptions ("type");
*/

@Entity
@Table(name = Subscription.DB_TABLE)
public class Subscription {
    public static final String DB_TABLE = "gv_subscriptions";

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

    @ManyToOne
    @JoinColumn(name = "org_id", nullable = false)
    private Organization organization;

    @Column(name = "type", nullable = false)
    private String type;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "start_date", nullable = false)
    private Instant startDate;

    @Column(name = "end_date", nullable = false)
    private Instant endDate;

    @Column(name = "contract_number", nullable = true)
    private String contractNumber;

    @Column(name = "account_number", nullable = true)
    private String accountNumber;

    @Column(name = "order_number", nullable = true)
    private String orderNumber;

    // @OneToMany(cascade = CascadeType.ALL)
    // @JoinTable(name = "gv_subscription_pools", joinColumns = @JoinColumn(name = "subscription_id"))
    // private List<Pool> pools;

    public Subscription() {
        // intentionally left empty
    }

    public String getId() {
        return this.id;
    }

    public Subscription setId(String id) {
        this.id = id;
        return this;
    }

    public Instant getCreated() {
        return this.created;
    }

    public Subscription setCreated(Instant created) {
        this.created = created;
        return this;
    }

    public Instant getUpdated() {
        return this.updated;
    }

    public Subscription setUpdated(Instant updated) {
        this.updated = updated;
        return this;
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

    public String getOid() {
        return this.oid;
    }

    public Subscription setOid(String oid) {
        this.oid = oid;
        return this;
    }

    public Organization getOrganization() {
        return this.organization;
    }

    public Subscription setOrganization(Organization organization) {
        this.organization = organization;
        return this;
    }

    public String getType() {
        return this.type;
    }

    public Subscription setType(String type) {
        this.type = type;
        return this;
    }

    public Product getProduct() {
        return this.product;
    }

    public Subscription setProduct(Product product) {
        this.product = product;
        return this;
    }

    public Instant getStartDate() {
        return this.startDate;
    }

    public Subscription setStartDate(Instant startDate) {
        this.startDate = startDate;
        return this;
    }

    public Instant getEndDate() {
        return this.endDate;
    }

    public Subscription setEndDate(Instant endDate) {
        this.endDate = endDate;
        return this;
    }

    public String getContractNumber() {
        return this.contractNumber;
    }

    public Subscription setContractNumber(String contractNumber) {
        this.contractNumber = contractNumber;
        return this;
    }

    public String getAccountNumber() {
        return this.accountNumber;
    }

    public Subscription setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
        return this;
    }

    public String getOrderNumber() {
        return this.orderNumber;
    }

    public Subscription setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
        return this;
    }

    // public List<Pool> getPools() {
    //     return this.pools;
    // }

    // public Subscription setPools(List<Pool> pools) {
    //     this.pools = pools;
    //     return this;
    // }

}
