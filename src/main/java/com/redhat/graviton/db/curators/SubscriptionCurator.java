package com.redhat.graviton.db.curators;

import com.redhat.graviton.db.model.*;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import java.util.List;


@ApplicationScoped
public class SubscriptionCurator {

    @Inject
    private Provider<EntityManager> entityManagerProvider;

    public SubscriptionCurator() {
        // intentionally left empty
    }

    public EntityManager getEntityManager() {
        return this.entityManagerProvider.get();
    }

    public Subscription persist(Subscription entity) {
        if (entity == null) {
            throw new IllegalArgumentException("entity is null");
        }

        this.getEntityManager()
            .persist(entity);

        return entity;
    }

    public Subscription merge(Subscription entity) {
        if (entity == null) {
            throw new IllegalArgumentException("entity is null");
        }

        return this.getEntityManager()
            .merge(entity);
    }

    public Organization persist(Organization entity) {
        if (entity == null) {
            throw new IllegalArgumentException("entity is null");
        }

        this.getEntityManager()
            .persist(entity);

        return entity;
    }

    public Organization getOrganizationByOid(String orgOid) {
        String jpql = "SELECT org FROM Organization org WHERE org.oid = :oid";

        try {
            return this.getEntityManager()
                .createQuery(jpql, Organization.class)
                .setParameter("oid", orgOid)
                .getSingleResult();
        }
        catch (NoResultException nre) {
            return null;
        }
    }

    public List<Subscription> getSubscriptionByOrg(String orgId) {
        String jpql = "SELECT sub FROM Subscription sub WHERE sub.organization.id = :orgId";

        return this.getEntityManager()
            .createQuery(jpql, Subscription.class)
            .setParameter("orgId", orgId)
            .getResultList();
    }

}
