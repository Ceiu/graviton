package com.redhat.graviton.db.model;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.PrePersist;


/*
    -- key pairs (immutable)
    CREATE TABLE IF NOT EXISTS gv_key_pairs (
      "id" VARCHAR(64) NOT NULL,
      "created" TIMESTAMP NOT NULL DEFAULT now(),
      "public_key" TEXT NOT NULL,
      "private_key" TEXT NOT NULL,

      PRIMARY KEY ("id")
    );
*/

@Entity
@Table(name = KeyPairData.DB_TABLE)
public class KeyPairData {
    public static final String DB_TABLE = "gv_key_pairs";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "created", nullable = false)
    private Instant created;

    @Column(name = "public_key", nullable = false)
    private String publicKey;

    @Column(name = "private_key", nullable = false)
    private String privateKey;

    public KeyPairData() {
        // intentionally left empty
    }

    public String getId() {
        return this.id;
    }

    public KeyPairData setId(String id) {
        this.id = id;
        return this;
    }

    public Instant getCreated() {
        return this.created;
    }

    public KeyPairData setCreated(Instant created) {
        this.created = created;
        return this;
    }

    @PrePersist
    protected void onCreate() {
        if (this.getCreated() == null) {
            this.setCreated(Instant.now());
        }
    }

    public String getPublicKey() {
        return this.publicKey;
    }

    public KeyPairData setPublicKey(String publicKey) {
        this.publicKey = publicKey;
        return this;
    }

    public String getPrivateKey() {
        return this.privateKey;
    }

    public KeyPairData setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
        return this;
    }

}
