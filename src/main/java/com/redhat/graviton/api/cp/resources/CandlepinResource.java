package com.redhat.graviton.api.cp.resources;

import com.redhat.graviton.api.cp.cert.*;
import com.redhat.graviton.api.cp.model.*;
import com.redhat.graviton.api.poc.model.*;
import com.redhat.graviton.api.datasource.model.*;
import com.redhat.graviton.db.curators.*;
import com.redhat.graviton.db.model.*;
import com.redhat.graviton.sync.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.jboss.logging.Logger;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.MediaType;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.DeflaterOutputStream;
import java.util.stream.*;




@Path("/candlepin")
public class CandlepinResource {
    private static final Logger LOG = Logger.getLogger(CandlepinResource.class);


    @Inject
    private ConsumerCurator consumerCurator;

    @Inject
    private OrganizationCurator orgCurator;

    @Inject
    private ProductCurator productCurator;

    @GET
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> candlepinStatus() {

        List<String> capabilities = List.of("instance_multiplier", "derived_product", "vcpu", "cert_v3",
            "hypervisors_heartbeat", "remove_by_pool_id", "syspurpose", "storage_band", "cores",
            "multi_environment", "hypervisors_async", "org_level_content_access", "guest_limit", "ram",
            "batch_bind");

        Map<String, Object> status = new HashMap<>();
        status.put("mode", "NORMAL");
        status.put("modeReason", null);
        status.put("modeChangeTime", null);
        status.put("result", true); // ????
        status.put("version", "5.0.0");
        status.put("release", "1");
        status.put("standalone", true);
        status.put("timeUTC", Instant.now());
        status.put("rulesSource", "default");
        status.put("rulesVersion", "5.44");
        status.put("managerCapabilities", capabilities);
        status.put("keycloakRealm", null);
        status.put("keycloakAuthUrl", null);
        status.put("keycloakResource", null);
        status.put("deviceAuthRealm", null);
        status.put("deviceAuthUrl", null);
        status.put("deviceAuthClientId", null);
        status.put("deviceAuthScope", null);

        return status;
    }


    @GET
    @Path("/owners")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Organization> listOrgs() {
        return this.orgCurator.listOrgs();
    }

    @GET
    @Path("/owners/{org_oid}/products")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Product> listProductsByOrg(
        @PathParam("org_oid") String orgOid) {

        return this.productCurator.getProductsByOrgOid(orgOid);
    }

    @GET
    @Path("/products")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Product> listProducts() {
        return this.productCurator.listProducts();
    }

}
