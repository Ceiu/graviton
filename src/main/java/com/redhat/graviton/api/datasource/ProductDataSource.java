package com.redhat.graviton.api.datasource;

import com.redhat.graviton.api.datasource.model.ExtProduct;
import com.redhat.graviton.api.datasource.model.ExtProductChildren;

import java.util.Collection;
import java.util.Map;




public interface ProductDataSource {

    public Map<String, ExtProduct> getProducts();
    public Map<String, ExtProduct> getProducts(Collection<String> oids);
    public Map<String, ExtProduct> getProducts(String... oids);
    public ExtProduct getProduct(String oid);

    public Map<String, ExtProductChildren> getProductChildren();
    public Map<String, ExtProductChildren> getProductChildren(Collection<String> oids);
    public Map<String, ExtProductChildren> getProductChildren(String... oids);
    public ExtProductChildren getProductChildren(String oid);

}
