package com.redhat.graviton.db.curators;

import com.redhat.graviton.db.model.*;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import java.util.List;


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


}
