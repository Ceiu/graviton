package com.redhat.graviton.db.model;

import java.time.Instant;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/*
  TODO: model
*/

@Entity
@Table(name = Consumer.DB_TABLE)
public class Consumer {
    public static final String DB_TABLE = "gv_consumers";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private String id;

    public String getId() {
        return this.id;
    }

    public Consumer() {
        // FIX ME
    }

    public Consumer setId(String id) {
        this.id = id;
        return this;
    }

}
