package com.redhat.graviton.db.curators;

import com.redhat.graviton.db.model.*;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import java.util.List;


@Singleton
public class ConsumerCurator extends AbstractCurator {

    @Inject
    public ConsumerCurator(Provider<EntityManager> entityManagerProvider) {
        super(entityManagerProvider);
    }

    public List<Consumer> listConsumers() {
        String jpql = "SELECT consumer FROM Consumer consumer";

        return this.getEntityManager()
            .createQuery(jpql, Consumer.class)
            .getResultList();
    }

    public Consumer getConsumerById(String id) {
        return this.getEntityManager().find(Consumer.class, id);
    }

    public Consumer getConsumerByOid(String oid) {
        try {
            String jpql = "SELECT consumer FROM Consumer consumer WHERE consumer.oid = :oid";

            return this.getEntityManager()
                .createQuery(jpql, Consumer.class)
                .setParameter("oid", oid)
                .getSingleResult();
        }
        catch (NoResultException nre) {
            return null;
        }
    }

}
