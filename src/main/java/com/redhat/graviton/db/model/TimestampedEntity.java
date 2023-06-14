package com.redhat.graviton.db.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;



@MappedSuperclass
public abstract class TimestampedEntity<T extends TimestampedEntity<T>> {

    @Column(name = "created", nullable = false)
    private Instant created;

    @Column(name = "updated", nullable = false)
    private Instant updated;


    public TimestampedEntity() {
        // intentionally left empty
    }

    public Instant getCreated() {
        return this.created;
    }

    public T setCreated(Instant created) {
        this.created = created;
        return (T) this;
    }

    public Instant getUpdated() {
        return this.updated;
    }

    public T setUpdated(Instant updated) {
        this.updated = updated;
        return (T) this;
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

}
