package com.redhat.graviton.api.candlepin.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/*
    [2] Compliance status output in SCA mode:
    {
      "status" : "disabled",
      "compliant" : true,
      "date" : "2023-06-14T20:37:07+00:00",
      "compliantUntil" : null,
      "compliantProducts" : { },
      "partiallyCompliantProducts" : { },
      "partialStacks" : { },
      "nonCompliantProducts" : [ ],
      "reasons" : [ ],
      "productComplianceDateRanges" : { }
    }
*/
public class CPComplianceStatusDTO {

    private String status;
    private boolean compliant;
    private Instant date;
    private Instant compliantUntil;
    private Map<String, List<Object>> compliantProducts;
    private Map<String, List<Object>> partiallyCompliantProducts;
    private Map<String, List<Object>> partialStacks;
    private List<String> nonCompliantProducts;
    private List<Object> reasons;
    private Map<String, Object> productComplianceDateRanges;


    public CPComplianceStatusDTO() {
        // empty
    }


    public String getStatus() {
        return this.status;
    }

    public CPComplianceStatusDTO setStatus(String status) {
        this.status = status;
        return this;
    }

    public boolean getCompliant() {
        return this.compliant;
    }

    public CPComplianceStatusDTO setCompliant(boolean compliant) {
        this.compliant = compliant;
        return this;
    }

    public Instant getDate() {
        return this.date;
    }

    public CPComplianceStatusDTO setDate(Instant date) {
        this.date = date;
        return this;
    }

    public Instant getCompliantUntil() {
        return this.compliantUntil;
    }

    public CPComplianceStatusDTO setCompliantUntil(Instant compliantUntil) {
        this.compliantUntil = compliantUntil;
        return this;
    }

    public Map<String, List<Object>> getCompliantProducts() {
        return this.compliantProducts;
    }

    public CPComplianceStatusDTO setCompliantProducts(Map<String, List<Object>> compliantProducts) {
        this.compliantProducts = compliantProducts;
        return this;
    }

    public Map<String, List<Object>> getPartiallyCompliantProducts() {
        return this.partiallyCompliantProducts;
    }

    public CPComplianceStatusDTO setPartiallyCompliantProducts(Map<String, List<Object>> partiallyCompliantProducts) {
        this.partiallyCompliantProducts = partiallyCompliantProducts;
        return this;
    }

    public Map<String, List<Object>> getPartialStacks() {
        return this.partialStacks;
    }

    public CPComplianceStatusDTO setPartialStacks(Map<String, List<Object>> partialStacks) {
        this.partialStacks = partialStacks;
        return this;
    }

    public List<String> getNonCompliantProducts() {
        return this.nonCompliantProducts;
    }

    public CPComplianceStatusDTO setNonCompliantProducts(List<String> nonCompliantProducts) {
        this.nonCompliantProducts = nonCompliantProducts;
        return this;
    }

    public List<Object> getReasons() {
        return this.reasons;
    }

    public CPComplianceStatusDTO setReasons(List<Object> reasons) {
        this.reasons = reasons;
        return this;
    }

    public Map<String, Object> getProductComplianceDateRanges() {
        return this.productComplianceDateRanges;
    }

    public CPComplianceStatusDTO setProductComplianceDateRanges(Map<String, Object> productComplianceDateRanges) {
        this.productComplianceDateRanges = productComplianceDateRanges;
        return this;
    }

}
