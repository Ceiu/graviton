package com.redhat.graviton.db.curators;

import com.redhat.graviton.db.model.*;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import java.util.List;
import java.util.Random;


@Singleton
public class OrganizationCurator extends AbstractCurator {

    @Inject
    public OrganizationCurator(Provider<EntityManager> entityManagerProvider) {
        super(entityManagerProvider);
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

    public List<Organization> listOrgs() {
        return this.getEntityManager()
            .createQuery("SELECT org FROM Organization org", Organization.class)
            .getResultList();
    }


    public List<Organization> listRandomOrgs(int count, int seed) {
        EntityManager entityManager = this.getEntityManager();

        String seedsql = "SELECT setseed(:seed)";

        // postgresql needs a seed value between -1.0 and 1.0, so use our integer seed to generate
        // a random floating value to throw at it instead.
        float fseed = new Random(seed).nextFloat();

        entityManager.createNativeQuery(seedsql)
            .setParameter("seed", fseed)
            .getSingleResult();

        String sql = "SELECT org.id FROM gv_organizations org ORDER BY RANDOM() LIMIT :count";

        List<String> orgIds = this.getEntityManager()
            .createNativeQuery(sql, String.class)
            .setParameter("count", count)
            .getResultList();

        String jpql = "SELECT org FROM Organization org WHERE org.id IN (:org_ids)";

        return this.getEntityManager()
            .createQuery(jpql, Organization.class)
            .setParameter("org_ids", orgIds)
            .getResultList();
    }


}
