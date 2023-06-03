package com.redhat.graviton.api.datasource;

import com.redhat.graviton.api.datasource.model.*;

import java.util.Collection;
import java.util.List;




public interface SubscriptionDataSource {

    public List<UpstreamSubscription> getSubscriptions(String orgId);

    public List<UpstreamSubscription> getSubscriptionsByOids(String orgId, Collection<String> subOids);

    // public UpstreamSubscription getSubscription(String subscriptionId); // no. not viable with the test data we have to work with

}
