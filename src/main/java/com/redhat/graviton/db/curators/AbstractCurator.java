package com.redhat.graviton.db.curators;

import com.redhat.graviton.db.model.*;

import jakarta.inject.Provider;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;


public class AbstractCurator {

    private final Provider<EntityManager> entityManagerProvider;

    public AbstractCurator(Provider<EntityManager> entityManagerProvider) {
        this.entityManagerProvider = Objects.requireNonNull(entityManagerProvider);
    }

    public EntityManager getEntityManager() {
        return this.entityManagerProvider.get();
    }

    public <T> T persist(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("entity is null");
        }

        this.getEntityManager()
            .persist(entity);

        return entity;
    }

    public <T> T merge(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("entity is null");
        }

        return this.getEntityManager()
            .merge(entity);
    }

    public <T> T remove(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("entity is null");
        }

        this.getEntityManager()
            .remove(entity);

        return entity;
    }

    public <T> List<List<T>> partition(Collection<T> collection, int blockSize) {
        List<T> base = collection instanceof List ? (List<T>) collection : new ArrayList<>(collection);
        List<List<T>> chunks = new ArrayList<>();

        for (int offset = 0; offset <= base.size(); offset += blockSize) {
            int idx = offset + blockSize;
            if (idx > base.size()) {
                idx = base.size();
            }

            chunks.add(base.subList(offset, idx));
        }

        return chunks;
    }

    public <T> List<List<T>> partition(Collection<T> collection) {
        int blockSize = 32000; // FIXME: hard coded because I'm too lazy to do this right at the moment
        return this.partition(collection, blockSize);
    }
}
