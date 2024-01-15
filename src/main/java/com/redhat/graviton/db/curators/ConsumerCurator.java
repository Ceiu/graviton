package com.redhat.graviton.db.curators;

import com.redhat.graviton.db.model.*;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;

import java.util.Collection;
import java.util.List;
import java.util.Map;


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


    public List<Consumer> findConsumersWithAnyFacts(Collection<String> facts) {
        String jpql = "SELECT c FROM Consumer c JOIN c.facts f WHERE key(f) IN (:facts)";

        return this.getEntityManager()
            .createQuery(jpql, Consumer.class)
            .setParameter("facts", facts)
            .getResultList();
    }

    public List<Consumer> findConsumersWithAllFacts(Collection<String> facts) {
        if (facts == null || facts.isEmpty()) {
            throw new IllegalArgumentException("no facts specified");
        }

        if (facts.size() > 1000) {
            throw new UnsupportedOperationException("consumer query does not support more than 1000 facts");
        }

        StringBuilder jpql = new StringBuilder("SELECT c FROM Consumer c JOIN c.facts f0");

        for (int i = 1; i < facts.size(); ++i) {
            jpql.append(" JOIN c.facts f")
                .append(i);
        }

        jpql.append(" WHERE key(f0) = :fact0");

        for (int i = 1; i < facts.size(); ++i) {
            jpql.append(" AND key(f")
                .append(i)
                .append(") = :fact")
                .append(i);
        }

        Query query = this.getEntityManager()
            .createQuery(jpql.toString(), Consumer.class);

        int i = 0;
        for (String fact : facts) {
            query.setParameter("fact" + i++, fact);
        }

        return query.getResultList();
    }

    public List<Consumer> findConsumersWithFacts(Map<String, String> facts) {
        if (facts == null || facts.isEmpty()) {
            throw new IllegalArgumentException("no facts specified");
        }

        if (facts.size() > 1000) {
            throw new UnsupportedOperationException("consumer query does not support more than 1000 facts");
        }

        StringBuilder jpql = new StringBuilder("SELECT c FROM Consumer c JOIN c.facts f0");

        for (int i = 1; i < facts.size(); ++i) {
            jpql.append(" JOIN c.facts f")
                .append(i);
        }

        jpql.append(" WHERE key(f0) = :fact0 AND value(f0) = :value0");

        for (int i = 1; i < facts.size(); ++i) {
            jpql.append(" AND key(f")
                .append(i)
                .append(") = :fact")
                .append(i)
                .append(" AND value(f")
                .append(i)
                .append(") = :value")
                .append(i);
        }

        Query query = this.getEntityManager()
            .createQuery(jpql.toString(), Consumer.class);

        int i = 0;
        for (Map.Entry<String, String> kvpair : facts.entrySet()) {
            query.setParameter("fact" + i, kvpair.getKey())
                .setParameter("value" + i, kvpair.getValue());

            ++i;
        }

        return query.getResultList();
    }


    // JSON B IMPL

    // 19510
    // public List<Consumer> findConsumersWithAnyFacts(Collection<String> facts) {
    //     // TODO: validate and partition facts as necessary

    //     String sql = new StringBuilder("SELECT c.* FROM gv_consumers c WHERE c.facts \\?\\?| array[?")
    //         .append(", ?".repeat(facts.size() - 1))
    //         .append(']')
    //         .toString();

    //     Query query = this.getEntityManager()
    //         .createNativeQuery(sql, Consumer.class);

    //     int index = 0;
    //     for (String elem : facts) {
    //         query.setParameter(++index, elem);
    //     }

    //     return query.getResultList();
    // }

    // 18653
    // public List<Consumer> findConsumersWithAllFacts(Collection<String> facts) {
    //     // TODO: validate and partition facts as necessary

    //     String sql = new StringBuilder("SELECT c.* FROM gv_consumers c WHERE c.facts \\?\\?& array[?")
    //         .append(", ?".repeat(facts.size() - 1))
    //         .append(']')
    //         .toString();

    //     Query query = this.getEntityManager()
    //         .createNativeQuery(sql, Consumer.class);

    //     int index = 0;
    //     for (String elem : facts) {
    //         query.setParameter(++index, elem);
    //     }

    //     return query.getResultList();
    // }

    // 7082
    // public List<Consumer> findConsumersWithFacts(Map<String, String> facts) {
    //     if (facts == null || facts.isEmpty()) {
    //         throw new IllegalArgumentException("no facts specified");
    //     }

    //     if (facts.size() > 1000) {
    //         throw new UnsupportedOperationException("consumer query does not support more than 1000 facts");
    //     }

    //     StringBuilder sql = new StringBuilder("SELECT c.* FROM gv_consumers c WHERE (facts ->> :key_0) = :value_0");

    //     for (int i = 1; i < facts.size(); ++i) {
    //         sql.append(" AND (facts ->> :key_")
    //             .append(i)
    //             .append(") = :value_")
    //             .append(i);
    //     }

    //     Query query = this.getEntityManager()
    //         .createNativeQuery(sql.toString(), Consumer.class);

    //     int index = 0;
    //     for (Map.Entry<String, String> fact : facts.entrySet()) {
    //         query.setParameter("key_" + index, fact.getKey())
    //             .setParameter("value_" + index, fact.getValue());

    //         ++index;
    //     }

    //     return query.getResultList();
    // }

}
