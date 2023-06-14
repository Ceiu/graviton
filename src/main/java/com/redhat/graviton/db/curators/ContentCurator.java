package com.redhat.graviton.db.curators;

import com.redhat.graviton.db.model.*;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;

import java.util.List;


@Singleton
public class ContentCurator extends AbstractCurator {

    @Inject
    public ContentCurator(Provider<EntityManager> entityManagerProvider) {
        super(entityManagerProvider);
    }

    public List<Content> listContent() {
        return this.getEntityManager()
            .createQuery("SELECT c FROM Content c", Content.class)
            .getResultList();
    }

}
