package com.redhat.graviton.api.datasource;

import com.redhat.graviton.api.datasource.model.UpstreamProduct;
import com.redhat.graviton.api.datasource.model.UpstreamProductTree;

import java.util.Collection;
import java.util.Map;




public interface ProductDataSource {

    public Map<String, UpstreamProduct> getProducts();
    public Map<String, UpstreamProduct> getProducts(Collection<String> oids);
    public Map<String, UpstreamProduct> getProducts(String... oids);
    public UpstreamProduct getProduct(String oid);

    public Map<String, UpstreamProductTree> getProductTrees();
    public Map<String, UpstreamProductTree> getProductTrees(Collection<String> oids);
    public Map<String, UpstreamProductTree> getProductTrees(String... oids);
    public UpstreamProductTree getProductTree(String oid);

}
