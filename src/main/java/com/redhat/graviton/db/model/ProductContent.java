package com.redhat.graviton.db.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


/*
    -- product content
    CREATE TABLE IF NOT EXISTS gv_product_contents (
      "product_id" VARCHAR(64) NOT NULL,
      "content_id" VARCHAR(64) NOT NULL,

      PRIMARY KEY ("product_id", "content_id"),
      FOREIGN KEY ("product_id") REFERENCES gv_products ("id") ON DELETE CASCADE,
      FOREIGN KEY ("content_id") REFERENCES gv_contents ("id")
    );
    CREATE INDEX IF NOT EXISTS gv_product_contents_idx1 ON gv_product_contents ("product_id");
*/

@Entity
@Table(name = ProductContent.DB_TABLE)
public class ProductContent {
    public static final String DB_TABLE = "gv_product_contents";

    @Id
    @Column(name = "product_id", nullable = false)
    private String productId;

    @Id
    @Column(name = "content_id", nullable = false)
    private String contentId;


    public ProductContent() {

    }

    public String getProductId() {
        return this.productId;
    }

    public ProductContent setProductId(String productId) {
        this.productId = productId;
        return this;
    }

    public String getContentId() {
        return this.contentId;
    }

    public ProductContent setContentId(String contentId) {
        this.contentId = contentId;
        return this;
    }

    public String toString() {
        return String.format("ProductContent [pid: %s, cid: %s]", this.getProductId(), this.getContentId());
    }


}
