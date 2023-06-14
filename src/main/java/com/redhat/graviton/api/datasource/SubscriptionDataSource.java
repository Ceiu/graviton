package com.redhat.graviton.api.datasource;

import com.redhat.graviton.api.datasource.model.ExtSubscription;

import java.util.Collection;
import java.util.List;




public interface SubscriptionDataSource {

    public List<ExtSubscription> getSubscriptions(String orgId);

    public List<ExtSubscription> getSubscriptionsByIds(String orgId, Collection<String> subscriptionIds);

    public List<ExtSubscription> getSubscriptionsByIds(String orgId, String... subscriptionIds);

    // public ExtSubscription getSubscriptionById(String subscriptionId);

}
