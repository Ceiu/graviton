package com.redhat.graviton.db.curators;

import com.redhat.graviton.db.model.*;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import java.util.List;


@ApplicationScoped
public class ConsumerCurator {

    @Inject
    private Provider<EntityManager> entityManagerProvider;

    public ConsumerCurator() {
        // intentionally left empty
    }

    public EntityManager getEntityManager() {
        return this.entityManagerProvider.get();
    }

    public Consumer persist(Consumer entity) {
        if (entity == null) {
            throw new IllegalArgumentException("entity is null");
        }

        this.getEntityManager()
            .persist(entity);

        return entity;
    }

    public Consumer merge(Consumer entity) {
        if (entity == null) {
            throw new IllegalArgumentException("entity is null");
        }

        return this.getEntityManager()
            .merge(entity);
    }

    public Organization getOrgById(String id) {
        return this.getEntityManager().find(Organization.class, id);
    }

    public Organization getOrgByOid(String oid) {
        try {
            String jpql = "SELECT org FROM Organization org WHERE org.oid = :oid";

            return this.getEntityManager()
                .createQuery(jpql, Organization.class)
                .setParameter("oid", oid)
                .getSingleResult();

        }
        catch (NoResultException nre) {
            return null;
        }

    }


}
