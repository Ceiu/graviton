-- organizations
CREATE TABLE IF NOT EXISTS gv_organizations (
  "id" VARCHAR(64) NOT NULL,
  "created" TIMESTAMP NOT NULL DEFAULT now(),
  "updated" TIMESTAMP NOT NULL DEFAULT now(),
  "oid" VARCHAR(64) NOT NULL UNIQUE, -- "CP account, org_key, or upstream org ID"
  "name" VARCHAR(256) NOT NULL,
  -- "parent_org_id" VARCHAR(64),

  PRIMARY KEY ("id")
  -- FOREIGN KEY ("parent_org_id") REFERENCES gv_organizations ("id")
);
CREATE INDEX IF NOT EXISTS gv_organizations_idx1 ON gv_organizations ("oid");


-- certificates (immutable)
CREATE TABLE IF NOT EXISTS gv_certificates (
  "id" VARCHAR(64) NOT NULL,
  "created" TIMESTAMP NOT NULL DEFAULT now(),
  "serial" BIGINT NOT NULL,
  "valid_after" TIMESTAMP NOT NULL,
  "valid_until" TIMESTAMP NOT NULL,
  "private_key" TEXT NOT NULL,
  "certificate" TEXT NOT NULL,

  PRIMARY KEY ("id")
);
CREATE INDEX IF NOT EXISTS gv_certificates_idx1 ON gv_certificates ("serial");

-- key pairs (immutable)
CREATE TABLE IF NOT EXISTS gv_key_pairs (
  "id" VARCHAR(64) NOT NULL,
  "created" TIMESTAMP NOT NULL DEFAULT now(),
  "public_key" TEXT NOT NULL,
  "private_key" TEXT NOT NULL,

  PRIMARY KEY ("id")
);


-- contents
CREATE TABLE IF NOT EXISTS gv_contents (
  "id" VARCHAR(64) NOT NULL,
  "created" TIMESTAMP NOT NULL DEFAULT now(),
  "updated" TIMESTAMP NOT NULL DEFAULT now(),
  "oid" VARCHAR(64) NOT NULL UNIQUE,
  "name" VARCHAR(256) NOT NULL,
  "type" VARCHAR(32) NOT NULL,
  "enabled" BOOLEAN NOT NULL DEFAULT false,
  "label" VARCHAR(256),
  "arches" VARCHAR(256),
  "vendor" VARCHAR(256),
  "content_url" VARCHAR(256),
  "gpg_url" VARCHAR(256),
  "required_tags" VARCHAR(256),
  "release_version" VARCHAR(256),
  "metadata" VARCHAR(256),
  "metadata_expiration" INTEGER,

  PRIMARY KEY ("id")
);
CREATE INDEX IF NOT EXISTS gv_contents_idx1 ON gv_contents ("oid");

CREATE TABLE IF NOT EXISTS gv_content_required_products (
  "content_id" VARCHAR(64) NOT NULL,
  "product_oid" VARCHAR(256) NOT NULL,

  PRIMARY KEY ("content_id", "product_oid"),
  FOREIGN KEY ("content_id") REFERENCES gv_contents ("id") ON DELETE CASCADE
);



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
  "product_id" VARCHAR(64) NOT NULL,
  "name" VARCHAR(256) NOT NULL,
  "value" VARCHAR(256),

  PRIMARY KEY ("product_id", "name"),
  FOREIGN KEY ("product_id") REFERENCES gv_products ("id") ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS gv_product_attrib_idx1 ON gv_product_attributes ("product_id");
CREATE INDEX IF NOT EXISTS gv_product_attrib_idx2 ON gv_product_attributes ("name");

-- dependent products
CREATE TABLE IF NOT EXISTS gv_product_dependent_products (
  "product_id" VARCHAR(64) NOT NULL,
  "product_oid" VARCHAR(64) NOT NULL,

  PRIMARY KEY ("product_id", "product_oid"),
  FOREIGN KEY ("product_id") REFERENCES gv_products ("id") ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS gv_product_dependent_products_idx1 ON gv_product_dependent_products ("product_id");

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
CREATE INDEX IF NOT EXISTS gv_product_branding_idx1 ON gv_product_branding ("product_id");

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
CREATE TABLE IF NOT EXISTS gv_product_graph (
  "product_id" VARCHAR(64) NOT NULL,
  "child_product_id" VARCHAR(64) NOT NULL,
  "depth" INTEGER NOT NULL DEFAULT 1,
  "type" VARCHAR(32) NOT NULL,

  PRIMARY KEY ("product_id", "child_product_id", "depth"),
  FOREIGN KEY ("product_id") REFERENCES gv_products ("id") ON DELETE CASCADE,
  FOREIGN KEY ("child_product_id") REFERENCES gv_products ("id") ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS gv_product_graph_idx1 ON gv_product_graph ("product_id");
CREATE INDEX IF NOT EXISTS gv_product_graph_idx2 ON gv_product_graph ("child_product_id");

-- -- pools
-- CREATE TABLE IF NOT EXISTS gv_pools (
--   "id" VARCHAR(64) NOT NULL,
--   "created" TIMESTAMP NOT NULL DEFAULT now(),
--   "updated" TIMESTAMP NOT NULL DEFAULT now(),
--   "org_id" VARCHAR(64) NOT NULL,
--   "product_id" VARCHAR(64) NOT NULL,
--   "start_date" TIMESTAMP,
--   "end_date" TIMESTAMP,
--   "quantity" INTEGER,

--   PRIMARY KEY ("id"),
--   FOREIGN KEY ("org_id") REFERENCES gv_organizations ("id") ON DELETE CASCADE,
--   FOREIGN KEY ("product_id") REFERENCES gv_products ("id")
-- );
-- CREATE INDEX IF NOT EXISTS gv_pools_idx1 ON gv_pools ("org_id");
-- CREATE INDEX IF NOT EXISTS gv_pools_idx2 ON gv_pools ("product_id");

-- -- pool attributes
-- CREATE TABLE IF NOT EXISTS gv_pool_attributes (
--   --may need an ID here for Hibernates sake
--   "pool_id" VARCHAR(64) NOT NULL,
--   "name" VARCHAR(256) NOT NULL,
--   "value" VARCHAR(256),

--   PRIMARY KEY ("pool_id", "name"),
--   FOREIGN KEY ("pool_id") REFERENCES gv_pools ("id") ON DELETE CASCADE
-- );



-- subscriptions
CREATE TABLE IF NOT EXISTS gv_subscriptions (
  "id" VARCHAR(64) NOT NULL,
  "created" TIMESTAMP NOT NULL DEFAULT now(),
  "updated" TIMESTAMP NOT NULL DEFAULT now(),
  "oid" VARCHAR(64) NOT NULL UNIQUE, -- are subscription OIDs globally unique?
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

-- -- subscription pools
-- CREATE TABLE IF NOT EXISTS gv_subscription_pools (
--   "subscription_id" VARCHAR(64) NOT NULL,
--   "pool_id" VARCHAR(64) NOT NULL,

--   PRIMARY KEY ("subscription_id", "pool_id"),
--   FOREIGN KEY ("subscription_id") REFERENCES gv_subscriptions ("id") ON DELETE CASCADE,
--   FOREIGN KEY ("pool_id") REFERENCES gv_pools ("id")
-- );
-- CREATE INDEX IF NOT EXISTS gv_sub_pool_idx1 ON gv_subscription_pools ("subscription_id");
-- CREATE INDEX IF NOT EXISTS gv_sub_pool_idx2 ON gv_subscription_pools ("pool_id");



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

-- consumer facts (ugh... do something better here)
CREATE TABLE IF NOT EXISTS gv_consumer_facts (
  "consumer_id" VARCHAR(64) NOT NULL,
  "name" VARCHAR(256) NOT NULL,
  "value" VARCHAR(256),

  PRIMARY KEY ("consumer_id", "name"),
  FOREIGN KEY ("consumer_id") REFERENCES gv_consumers ("id") ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS gv_consumer_facts_idx1 ON gv_consumer_facts ("consumer_id");
CREATE INDEX IF NOT EXISTS gv_consumer_facts_idx2 ON gv_consumer_facts ("name");


-- sca cert junk
CREATE TABLE IF NOT EXISTS gv_sca_content_certs (
  "id" VARCHAR(64) NOT NULL,
  "created" TIMESTAMP NOT NULL DEFAULT now(),
  "updated" TIMESTAMP NOT NULL DEFAULT now(),
  "filter" VARCHAR(255) NOT NULL UNIQUE,
  "serial" BIGINT NOT NULL,
  "valid_after" TIMESTAMP NOT NULL,
  "valid_until" TIMESTAMP NOT NULL,
  "private_key" TEXT NOT NULL,
  "certificate" TEXT NOT NULL,
  "content_data" TEXT NOT NULL,

  PRIMARY KEY ("id")
);
CREATE INDEX IF NOT EXISTS gv_sca_content_certs_idx1 ON gv_sca_content_certs ("serial");
CREATE INDEX IF NOT EXISTS gv_sca_content_certs_idx2 ON gv_sca_content_certs ("filter");
