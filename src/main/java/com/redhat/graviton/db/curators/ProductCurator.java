package com.redhat.graviton.db.curators;

import com.redhat.graviton.db.model.*;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


@ApplicationScoped
public class ProductCurator {

    @Inject
    private Provider<EntityManager> entityManagerProvider;

    public ProductCurator() {
        // intentionally left empty
    }

    public EntityManager getEntityManager() {
        return this.entityManagerProvider.get();
    }



    public Product persist(Product entity) {
        if (entity == null) {
            throw new IllegalArgumentException("entity is null");
        }

        this.getEntityManager()
            .persist(entity);

        return entity;
    }

    public Product merge(Product entity) {
        if (entity == null) {
            throw new IllegalArgumentException("entity is null");
        }

        return this.getEntityManager()
            .merge(entity);
    }

    public ProductGraphNode persistGraphNode(ProductGraphNode entity) {
        if (entity == null) {
            throw new IllegalArgumentException("entity is null");
        }

        this.getEntityManager()
            .persist(entity);

        return entity;
    }

    public ProductGraphNode removeGraphNode(ProductGraphNode entity) {
        if (entity == null) {
            throw new IllegalArgumentException("entity is null");
        }

        String jpql = "DELETE FROM ProductGraphNode pgn WHERE " +
            "pgn.productId = :productId AND pgn.childProductId = :childId AND pgn.depth = :depth";

        this.getEntityManager()
            .createQuery(jpql)
            .setParameter("productId", entity.getProductId())
            .setParameter("childId", entity.getChildProductId())
            .setParameter("depth", entity.getDepth())
            .executeUpdate();

        return entity;
    }

    public ProductContent persistProductContentLink(ProductContent entity) {
        if (entity == null) {
            throw new IllegalArgumentException("entity is null");
        }

        this.getEntityManager()
            .persist(entity);

        return entity;
    }

    public ProductContent removeProductContentLink(ProductContent entity) {
        if (entity == null) {
            throw new IllegalArgumentException("entity is null");
        }

        this.getEntityManager()
            .remove(entity);

        return entity;
    }





    public List<Product> listProducts() {
        return this.getEntityManager()
            .createQuery("SELECT p FROM Product p", Product.class)
            .getResultList();
    }

    public Map<String, Product> getProductsByOids(Collection<String> oids) {
        Map<String, Product> result = new HashMap<>();

        String jpql = "SELECT p FROM Product p WHERE p.oid IN (:block)";

        TypedQuery<Product> query = this.getEntityManager()
            .createQuery(jpql, Product.class);

        for (List<String> oidBlock : this.partition(oids)) {
            query.setParameter("block", oidBlock)
                .getResultList()
                .forEach(prod -> result.put(prod.getOid(), prod));
        }

        return result;
    }

    public Set<ProductOidGraphNode> getProductOidRelationsByOids(Collection<String> oids) {
        Set<ProductOidGraphNode> result = new HashSet<>();

        String jpql = "SELECT new com.redhat.graviton.db.model.ProductOidGraphNode(pnode.oid, cnode.oid, pg.depth, pg.type) " +
            "FROM Product pnode " +
            "JOIN ProductGraphNode pg ON pg.productId = pnode.id " +
            "JOIN Product cnode ON cnode.id = pg.childProductId " +
            "WHERE pnode.oid IN (:block1) OR cnode.oid IN (:block2)";

        TypedQuery<ProductOidGraphNode> query = this.getEntityManager()
            .createQuery(jpql, ProductOidGraphNode.class);

        // TODO: ARBITRARY BLOCK SIZE, PLS FIX. QUERY MAY ALSO BE SLOW! ALSO FIX THAT TOO
        for (List<String> block : this.partition(oids, 15000)) {
            query.setParameter("block1", block)
                .setParameter("block2", block)
                .getResultList()
                .forEach(result::add);
        }

        return result;
    }

    public Set<String> getRelatedOidsByOids(Collection<String> oids) {
        Set<String> result = new HashSet<>();

        // parent refs
        String parentRefJpql = "SELECT rel.oid FROM Product prod " +
            "JOIN ProductGraphNode pg ON pg.childProductId = prod.id " +
            "JOIN Product rel ON rel.id = pg.productId " +
            "WHERE pg.depth != 0 AND prod.oid IN (:block)";

        // child refs & self
        String childRefJpql = "SELECT rel.oid FROM Product prod " +
            "JOIN ProductGraphNode pg ON pg.productId = prod.id " +
            "JOIN Product rel ON rel.id = pg.childProductId " +
            "WHERE prod.oid IN (:block)";

        EntityManager manager = this.getEntityManager();

        TypedQuery<String> prefQuery = manager.createQuery(parentRefJpql, String.class);
        TypedQuery<String> crefQuery = manager.createQuery(childRefJpql, String.class);

        for (List<String> oidBlock : this.partition(oids)) {
            prefQuery.setParameter("block", oidBlock)
                .getResultList()
                .forEach(result::add);

            crefQuery.setParameter("block", oidBlock)
                .getResultList()
                .forEach(result::add);
        }

        return result;
    }


    public Set<String> getParentOidsByOids(Collection<String> oids) {
        Set<String> result = new HashSet<>();

        String jpql = "SELECT rel.oid FROM Product prod " +
            "JOIN ProductGraphNode pg ON pg.childProductId = prod.id " +
            "JOIN Product rel ON rel.id = pg.productId " +
            "WHERE pg.depth != 0 AND prod.oid IN (:block)";

        TypedQuery<String> query = this.getEntityManager()
            .createQuery(jpql, String.class);

        for (List<String> oidBlock : this.partition(oids)) {
            query.setParameter("block", oidBlock)
                .getResultList()
                .forEach(result::add);
        }

        return result;
    }

    public Set<String> getChildOidsByOids(Collection<String> oids) {
        Set<String> result = new HashSet<>();

        String jpql = "SELECT rel.oid FROM Product prod " +
            "JOIN ProductGraphNode pg ON pg.productId = prod.id " +
            "JOIN Product rel ON rel.id = pg.childProductId " +
            "WHERE pg.depth != 0 AND prod.oid IN (:block)";

        TypedQuery<String> query = this.getEntityManager()
            .createQuery(jpql, String.class);

        for (List<String> oidBlock : this.partition(oids)) {
            result.addAll(query.setParameter("block", oidBlock)
                .getResultList());
        }

        return result;
    }

    public Set<ProductContentOidLink> getProductContentOidLinksByProductOids(Collection<String> oids) {
        Set<ProductContentOidLink> result = new HashSet<>();

        String jpql = "SELECT new com.redhat.graviton.db.model.ProductContentOidLink(p.oid, c.oid) " +
            "FROM Product p " +
            "JOIN ProductContent pc ON p.id = pc.productId " +
            "JOIN Content c ON c.id = pc.contentId " +
            "WHERE p.oid IN (:productOids)";

        TypedQuery<ProductContentOidLink> query = this.getEntityManager()
            .createQuery(jpql, ProductContentOidLink.class);

        for (List<String> oidBlock : this.partition(oids)) {
            result.addAll(query.setParameter("productOids", oidBlock)
                .getResultList());
        }

        return result;
    }

    // NOTE: These two queries (above and below) could be done in one query if we abuse Hibernate hard
    // enough.

    public Map<String, Content> getContentByProductOids(Collection<String> oids) {
        Map<String, Content> result = new HashMap<>();

        String jpql = "SELECT c FROM Product p " +
            "JOIN ProductContent pc ON pc.productId = p.id " +
            "JOIN Content c ON c.id = pc.contentId " +
            "WHERE p.oid IN (:productOids)";

        TypedQuery<Content> query = this.getEntityManager()
            .createQuery(jpql, Content.class);

        for (List<String> oidBlock : this.partition(oids)) {
            query.setParameter("productOids", oidBlock)
                .getResultList()
                .forEach(elem -> result.put(elem.getOid(), elem));
        }

        return result;
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
