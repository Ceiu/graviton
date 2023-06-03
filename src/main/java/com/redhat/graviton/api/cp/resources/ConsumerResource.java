package com.redhat.graviton.api.cp.resources;

import com.redhat.graviton.api.cp.model.CPConsumerDTO;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;



@Path("/candlepin/consumers")
public class ConsumerResource {

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public CPConsumerDTO register() {
        return new CPConsumerDTO().setValue("consumer " + Math.random());
    }

    @GET
    @Path("/{consumer_uuid}/accessible_content")
    @Produces(MediaType.APPLICATION_JSON)
    public String fetchContentAccessCertificate() {
        return "I should fetch a cert here";
    }
}
