package com.redhat.graviton.db.curators;

import com.redhat.graviton.db.model.*;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import java.util.List;


@Singleton
public class SubscriptionCurator extends AbstractCurator {

    @Inject
    public SubscriptionCurator(Provider<EntityManager> entityManagerProvider) {
        super(entityManagerProvider);
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
