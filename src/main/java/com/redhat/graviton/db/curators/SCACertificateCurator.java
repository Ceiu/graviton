package com.redhat.graviton.db.curators;

import com.redhat.graviton.db.model.*;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.ArrayList;
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

    public List<SCAContentCertificate> listSCAContentCertificateByFilterAndSerials(String filter, List<Long> serials) {
        List<SCAContentCertificate> certs = new ArrayList<>();

        String jpql = "SELECT cert FROM SCAContentCertificate cert WHERE cert.filter = :filter " +
            "AND cert.serial IN (:serials)";

        TypedQuery<SCAContentCertificate> query = this.getEntityManager()
            .createQuery(jpql, SCAContentCertificate.class)
            .setParameter("filter", filter);

        for (List<Long> block : this.partition(serials)) {
            certs.addAll(query.setParameter("serials", block)
                .getResultList());
        }

        return certs;
    }

}
