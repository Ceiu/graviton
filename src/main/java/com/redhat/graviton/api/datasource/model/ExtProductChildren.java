package com.redhat.graviton.api.datasource.model;

import java.util.Map;
import java.util.Set;



public interface ExtProductChildren {

    public String getProductId();

    public Set<String> getChildrenProductIds(String type);
    public Map<String, Set<String>> getChildrenProductIds();

}
