package com.redhat.graviton.api.datasource.model;

import java.util.Set;


public interface ExtContent {

    public String getId();
    public String getType();
    public String getLabel();
    public String getName();
    public String getVendor();
    public String getContentUrl();
    public String getGpgUrl();
    public String getRequiredTags();
    public String getArches();
    public String getReleaseVer();
    public Integer getMetadataExpiration();
    public Set<String> getRequiredProductIds();
    public Boolean isEnabled();

}
