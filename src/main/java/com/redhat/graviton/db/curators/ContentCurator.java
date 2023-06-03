package com.redhat.graviton.db.curators;

import com.redhat.graviton.db.model.*;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.persistence.EntityManager;

import java.util.List;


@ApplicationScoped
public class ContentCurator {

    @Inject
    private Provider<EntityManager> entityManagerProvider;

    public ContentCurator() {
        // intentionally left empty
    }

    public EntityManager getEntityManager() {
        return this.entityManagerProvider.get();
    }

    public Content persist(Content entity) {
        if (entity == null) {
            throw new IllegalArgumentException("entity is null");
        }

        this.getEntityManager()
            .persist(entity);

        return entity;
    }

    public Content merge(Content entity) {
        if (entity == null) {
            throw new IllegalArgumentException("entity is null");
        }

        return this.getEntityManager()
            .merge(entity);
    }

    public List<Content> listContent() {
        return this.getEntityManager()
            .createQuery("SELECT c FROM Content c", Content.class)
            .getResultList();
    }

}
