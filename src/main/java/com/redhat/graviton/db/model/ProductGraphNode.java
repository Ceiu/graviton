package com.redhat.graviton.db.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


/*
    -- product map (children products/product graph)
    CREATE TABLE IF NOT EXISTS gv_product_graph (
      "product_id" VARCHAR(64) NOT NULL,
      "child_product_id" VARCHAR(64) NOT NULL,
      "depth" INTEGER NOT NULL DEFAULT 1
      "type" VARCHAR(32) NOT NULL,

      PRIMARY KEY ("product_id", "child_product_id"),
      FOREIGN KEY ("product_id") REFERENCES gv_products ("id") ON DELETE CASCADE,
      FOREIGN KEY ("child_product_id") REFERENCES gv_products ("id") ON DELETE CASCADE
    );
    CREATE INDEX IF NOT EXISTS gv_product_graph_idx1 ON gv_product_graph ("product_id");
    CREATE INDEX IF NOT EXISTS gv_product_graph_idx2 ON gv_product_graph ("child_product_id");
*/

@Entity
@Table(name = ProductGraphNode.DB_TABLE)
public class ProductGraphNode {
    public static final String DB_TABLE = "gv_product_graph";

    @Id
    @Column(name = "product_id", nullable = false)
    private String productId;

    @Id
    @Column(name = "child_product_id", nullable = false)
    private String childProductId;

    @Id
    @Column(name = "depth", nullable = false)
    private int depth;

    @Column(name = "type", nullable = false)
    private String type;

    public ProductGraphNode() {

    }


    public String getProductId() {
        return this.productId;
    }

    public ProductGraphNode setProductId(String productId) {
        this.productId = productId;
        return this;
    }


    public String getChildProductId() {
        return this.childProductId;
    }

    public ProductGraphNode setChildProductId(String childProductId) {
        this.childProductId = childProductId;
        return this;
    }

    public int getDepth() {
        return this.depth;
    }

    public ProductGraphNode setDepth(int depth) {
        this.depth = depth;
        return this;
    }

    public String getType() {
        return this.type;
    }

    public ProductGraphNode setType(String type) {
        this.type = type;
        return this;
    }


    public String toString() {
        return String.format("ProductGraphNode [pid: %s, cid: %s, depth: %d, type: %s",
            this.getProductId(), this.getChildProductId(), this.getDepth(), this.getType());
    }

}


// notes:
// this table will have *at least* as many rows as the products table does, as each entry will need
// a self reference at depth 0 for the joins to work cleanly. There may be a query-based workaround
// for it, but I expect Hibernate will be a problem as it typically is if we don't make this as
// braindead as possible.
