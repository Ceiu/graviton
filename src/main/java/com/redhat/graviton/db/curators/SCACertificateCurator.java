package com.redhat.graviton.db.curators;

import com.redhat.graviton.db.model.*;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import java.util.List;


@Singleton
public class SCACertificateCurator extends AbstractCurator {

    @Inject
    public SCACertificateCurator(Provider<EntityManager> entityManagerProvider) {
        super(entityManagerProvider);
    }

    public SCAContentCertificate getSCACertificateById(String id) {
        return this.getEntityManager().find(SCAContentCertificate.class, id);
    }

    public SCAContentCertificate getSCACertificateByFilter(String filter) {
        try {
            String jpql = "SELECT cert FROM SCAContentCertificate cert WHERE cert.filter = :filter";

            return this.getEntityManager()
                .createQuery(jpql, SCAContentCertificate.class)
                .setParameter("filter", filter)
                .getSingleResult();
        }
        catch (NoResultException nre) {
            return null;
        }
    }

    public int deleteSCACertificateByFilter(String filter) {
        String jpql = "DELETE FROM SCAContentCertificate cert WHERE cert.filter = :filter";

        return this.getEntityManager()
            .createQuery(jpql, SCAContentCertificate.class)
            .setParameter("filter", filter)
            .executeUpdate();
    }

}
