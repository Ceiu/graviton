package com.redhat.graviton.api.candlepin.resources;

import com.redhat.graviton.api.candlepin.cert.*;
import com.redhat.graviton.api.candlepin.model.*;
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




    private CPResourceDTO buildResource(String rel, String href) {
        return new CPResourceDTO()
            .setRel(rel)
            .setHref(href);
    }

    private CPOwnerDTO convertToCPOwner(Organization organization) {
        if (organization == null) {
            return null;
        }

        return new CPOwnerDTO()
            .setId(organization.getId())
            .setKey(organization.getOid())
            .setDisplayName(organization.getName())
            .setContentAccessMode("org_environment");
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<CPResourceDTO> getRootResources() {
        List<CPResourceDTO> rootResources = new ArrayList<>();

        // rootResources.add(this.buildResource("entitlements", "/entitlements"));
        rootResources.add(this.buildResource("subscriptions", "/subscriptions"));
        // rootResources.add(this.buildResource("environments", "/environments"));
        // rootResources.add(this.buildResource("jobs", "/jobs"));
        // rootResources.add(this.buildResource("roles", "/roles"));
        // rootResources.add(this.buildResource("activation_keys", "/activation_keys"));
        rootResources.add(this.buildResource("admin", "/admin"));
        rootResources.add(this.buildResource("pools", "/pools"));
        rootResources.add(this.buildResource("owners", "/owners"));
        // rootResources.add(this.buildResource("rules", "/rules"));
        // rootResources.add(this.buildResource("cdn", "/cdn"));
        rootResources.add(this.buildResource("{owner}", "/hypervisors/{owner}"));
        rootResources.add(this.buildResource("content_overrides", "/consumers/{consumer_uuid}/content_overrides"));
        rootResources.add(this.buildResource("users", "/users"));
        rootResources.add(this.buildResource("content", "/content"));
        rootResources.add(this.buildResource("products", "/products"));
        rootResources.add(this.buildResource("consumertypes", "/consumertypes"));
        rootResources.add(this.buildResource("consumers", "/consumers"));
        // rootResources.add(this.buildResource("deleted_consumers", "/deleted_consumers"));
        // rootResources.add(this.buildResource("distributor_versions", "/distributor_versions"));
        // rootResources.add(this.buildResource("crl", "/crl"));
        rootResources.add(this.buildResource("{id}", "/serials/{id}"));
        rootResources.add(this.buildResource("status", "/status"));

        return rootResources;
    }


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
    public List<CPOwnerDTO> listOrgs() {
        return this.orgCurator.listOrgs()
            .stream()
            .map(this::convertToCPOwner)
            .collect(Collectors.toList());
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

    @GET
    @Path("/users/{username}/owners")
    @Produces(MediaType.APPLICATION_JSON)
    public List<CPOwnerDTO> listUserOwners(
        @PathParam("username") String username) {

        // we don't have users or permissions yet at all, so just select some random orgs using the
        // hash code of the username as the random seed

        return this.orgCurator.listRandomOrgs(3, username.hashCode())
            .stream()
            .map(this::convertToCPOwner)
            .collect(Collectors.toList());
    }

    @GET
    @Path("/owners/{org_oid}/environments")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Object> listOwnerEnvironments(
        @PathParam("org_oid") String orgOid) {

        // we never have environments, and this probably shouldn't even be called. Return an empty
        // list just to ensure we don't break subman.
        return List.of();
    }

}
