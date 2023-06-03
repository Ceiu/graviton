package com.redhat.graviton.sync;

import com.redhat.graviton.api.datasource.SubscriptionDataSource;
import com.redhat.graviton.db.model.*;
import com.redhat.graviton.db.curators.*;
import com.redhat.graviton.api.datasource.model.*;
import com.redhat.graviton.sync.graph.*;

import java.util.Set;
import java.util.HashSet;

import org.jboss.logging.Logger;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.enterprise.context.Dependent;
import jakarta.transaction.Transactional;

import java.io.File;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;


@Dependent
public class OrganizationSync {
    private static final Logger LOG = Logger.getLogger(OrganizationSync.class);

    private final SubscriptionDataSource subscriptionDataSource;
    private final SubscriptionCurator subscriptionCurator;
    private final ProductCurator productCurator;
    private final Provider<PersistenceVisitor> visitorProvider;

    private boolean createOrg;
    private String orgOid;
    private Set<String> subscriptionOids;

    @Inject
    public OrganizationSync(SubscriptionDataSource subscriptionDataSource, SubscriptionCurator subscriptionCurator,
        ProductCurator productCurator, Provider<PersistenceVisitor> visitorProvider) {

        this.subscriptionDataSource = Objects.requireNonNull(subscriptionDataSource);
        this.subscriptionCurator = Objects.requireNonNull(subscriptionCurator);
        this.productCurator = Objects.requireNonNull(productCurator);
        this.visitorProvider = Objects.requireNonNull(visitorProvider);

        this.orgOid = null;
        this.subscriptionOids = new HashSet<>();
    }

    public OrganizationSync setOrganizationOid(String oid) {
        this.orgOid = oid;
        return this;
    }

    public OrganizationSync addSubscriptionOids(Collection<String> oids) {
        this.subscriptionOids.addAll(oids);
        return this;
    }

    public OrganizationSync setCreateOrg(boolean create) {
        this.createOrg = create;
        return this;
    }




    private void mapUpstreamSubscriptions(Map<String, SubscriptionNode> subscriptionNodeMap, Organization org,
        Map<String, Product> productMap, Collection<UpstreamSubscription> subscriptions) {

        for (UpstreamSubscription subscription : subscriptions) {
            SubscriptionNode snode = subscriptionNodeMap.computeIfAbsent(subscription.getId(), (key) -> new SubscriptionNode(org, productMap))
                .setExternalEntity(subscription);
        }
    }

    private void mapLocalSubscriptions(Map<String, SubscriptionNode> subscriptionNodeMap, Organization org,
        Map<String, Product> productMap, Collection<Subscription> subscriptions) {

        for (Subscription subscription : subscriptions) {
            SubscriptionNode snode = subscriptionNodeMap.computeIfAbsent(subscription.getOid(), (key) -> new SubscriptionNode(org, productMap))
                .setLocalEntity(subscription);
        }
    }

    @Transactional
    public void execute() {
        Organization org = this.subscriptionCurator.getOrganizationByOid(this.orgOid);
        if (org == null) {
            if (this.createOrg) {
                LOG.debugf("Organization %s does not exist; creating it...", this.orgOid);

                org = new Organization()
                    .setOid(this.orgOid)
                    .setName(this.orgOid); // use the oid as the name, I guess?

                this.subscriptionCurator.persist(org);
            }
            else {
                LOG.debugf("Organization %s does not exist; nothing to sync", this.orgOid);
                return;
            }
        }

        LOG.debugf("Syncing subscription data for org %s...", org.getOid());

        Map<String, SubscriptionNode> subscriptionNodeMap = new HashMap<>();

        LOG.debug("FETCHING UPSTREAM SUBSCRIPTION DATA");
        List<UpstreamSubscription> upstreamSubscriptions = !this.subscriptionOids.isEmpty() ?
            this.subscriptionDataSource.getSubscriptionsByOids(this.orgOid, this.subscriptionOids) :
            this.subscriptionDataSource.getSubscriptions(this.orgOid);

        LOG.debugf("FETCHED %d UPSTREAM SUBSCRIPTIONS", upstreamSubscriptions.size());

        LOG.debug("FETCHING REFERENCED PRODUCTS...");
        Set<String> productOids = upstreamSubscriptions.stream()
            .map(UpstreamSubscription::getProductId)
            .collect(Collectors.toSet());

        Map<String, Product> productMap = this.productCurator.getProductsByOids(productOids);
        LOG.debugf("FETCHED %d REFERENCED PRODUCTS", productMap.size());

        // Ensure we actually *have* all of the products being referenced
        if (productMap.size() != productOids.size()) {
            productOids.removeAll(productMap.keySet());
            LOG.errorf("SUBSCRIPTIONS REFERENCE UNKNOWN PRODUCTS: %s", productOids);
            throw new IllegalStateException("One or more subscriptions references an unknown product");
        }

        LOG.debug("MAPPING UPSTREAM SUBSCRIPTIONS...");
        this.mapUpstreamSubscriptions(subscriptionNodeMap, org, productMap, upstreamSubscriptions);

        LOG.debugf("FETCHING LOCAL SUBSCRIPTIONS FOR ORG %s", org.getOid());
        List<Subscription> localSubscriptions = this.subscriptionCurator.getSubscriptionByOrg(org.getId());

        LOG.debugf("FETCHED %d LOCAL SUBSCRIPTIONS", localSubscriptions.size());

        LOG.debug("MAPPING LOCAL SUBSCRIPTIONS...");
        this.mapLocalSubscriptions(subscriptionNodeMap, org, productMap, localSubscriptions);


        LOG.debug("PROCESSING NODES...");
        subscriptionNodeMap.values().forEach(GraphElement::process);

        // apply changes
        LOG.debug("WALKING NODE GRAPH...");
        PersistenceVisitor visitor = this.visitorProvider.get();
        Stack<GraphElement> path = new Stack<GraphElement>();
        subscriptionNodeMap.values().forEach(elem -> elem.walk(path, visitor));

        LOG.debugf("WALK COMPLETE. COUNTS: %s", visitor.getCounts());

        LOG.debug("COMMITTING TRANSACTION...");
    }

}
