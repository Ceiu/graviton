package com.redhat.graviton.api.datasource.model;

import java.time.Instant;



public interface ExtSubscription {

    public String getId();
    public String getOrganizationId();
    public String getType();
    public Instant getStartDate();
    public Instant getEndDate();
    public String getProductId();
    public String getContractNumber();
    public String getAccountNumber();
    public String getOrderNumber();

}
