package com.redhat.graviton.api.candlepin.resources;

import com.redhat.graviton.api.candlepin.cert.*;
import com.redhat.graviton.api.candlepin.model.*;
import com.redhat.graviton.api.datasource.model.*;
import com.redhat.graviton.db.curators.*;
import com.redhat.graviton.db.model.*;
import com.redhat.graviton.sync.*;
import com.redhat.graviton.certs.CertificateAuthority;
import com.redhat.graviton.certs.KeyGenerator;
import com.redhat.graviton.certs.PKIUtility;
import com.redhat.graviton.certs.X509CertificateBuilder;
import com.redhat.graviton.certs.X509StringExtension;
import com.redhat.graviton.certs.SCACertificateGenerator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.jboss.logging.Logger;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
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
import java.security.Signature;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;




@Path("/candlepin/consumers")
public class ConsumerResource {
    private static final Logger LOG = Logger.getLogger(ConsumerResource.class);

    @Inject
    private ObjectMapper mapper;

    @Inject
    private ConsumerCurator consumerCurator;

    @Inject
    private OrganizationCurator orgCurator;

    @Inject
    private ProductCurator productCurator;

    @Inject
    private SCACertificateCurator scaCertCurator;

    @Inject
    private CertificateAuthority certAuthority;

    @Inject
    private KeyGenerator keyGenerator;

    @Inject
    private PKIUtility pkiUtil;

    @Inject
    private Provider<X509CertificateBuilder> certBuilderProvider;

    @Inject
    private Provider<SCACertificateGenerator> scaCertGeneratorProvider;



    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Stream<CPConsumerDTO> listConsumers() {
        return this.consumerCurator.listConsumers()
            .stream()
            .map(this::convertToConsumerDTO);
    }

    @GET
    @Path("/{consumer_oid}")
    @Produces(MediaType.APPLICATION_JSON)
    public CPConsumerDTO getConsumer(
        @PathParam("consumer_oid") String consumerOid) {

        Consumer consumer = this.consumerCurator.getConsumerByOid(consumerOid);
        if (consumer == null) {
            LOG.errorf("no such consumer: %s", consumerOid);
            throw new NotFoundException("No such consumer: " + consumerOid);
        }

        return this.convertToConsumerDTO(consumer);
    }

    @GET
    @Path("/{consumer_oid}/compliance")
    @Produces(MediaType.APPLICATION_JSON)
    public CPComplianceStatusDTO getConsumerComplianceStatus(
        @PathParam("consumer_oid") String consumerOid) {

        return new CPComplianceStatusDTO()
            .setStatus("disabled")
            .setCompliant(true)
            .setDate(Instant.now())
            .setCompliantUntil(null)
            .setCompliantProducts(Map.of())
            .setPartiallyCompliantProducts(Map.of())
            .setPartialStacks(Map.of())
            .setNonCompliantProducts(List.of())
            .setReasons(List.of())
            .setProductComplianceDateRanges(Map.of());
    }


    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public CPConsumerDTO register(CPConsumerDTO dto,
        @QueryParam("username") String username,
        @QueryParam("owner") String orgOid,
        @QueryParam("activation_keys") String activationKeys,
        @QueryParam("identity_cert_creation") @DefaultValue("true") Boolean identityCertCreation) {

        if (dto == null) {
            throw new BadRequestException("no consumer provided??");
        }

        LOG.infof("Received new registration request for: %s, %s, %s", dto.getName(), username, orgOid);

        // If the user didn't specify an org, check the dto
        if (orgOid == null || orgOid.isEmpty()) {
            orgOid = dto.getOwner() != null ? dto.getOwner().getKey() : null;

            if (orgOid == null || orgOid.isEmpty()) {
                throw new BadRequestException("no organization specified");
            }
        }

        LOG.infof("Looking up org by oid: %s", orgOid);

        Organization org = this.orgCurator.getOrgByOid(orgOid);
        if (org == null) {
            LOG.errorf("No such organization: %s", orgOid);
            throw new NotFoundException("no such org: " + orgOid);
        }

        LOG.infof("Converting DTO to a real consumer");

        // convert dto to consumer
        Consumer consumer = this.convertToConsumer(org, dto, username);

        LOG.infof("Generating key pair for consumer %s", consumer);

        // create a keypair for this consumer
        KeyPairData kpdata = this.generateConsumerKeyPair(consumer);

        LOG.infof("Generated key pair for consumer: %s, %s", kpdata.getPublicKey(), kpdata.getPrivateKey());

        LOG.infof("Generating certificate for consumer %s", consumer);

        // create an identity cert for them
        Certificate certificate = this.generateIdentityCert(consumer);

        LOG.infof("Checking our work...");

        if (!kpdata.getPrivateKey().equals(certificate.getPrivateKey())) {
            LOG.errorf("Private key mismatch??\nkpdata.private_key:\n%s\n\ncertificate.private_key:\n%s",
                kpdata.getPrivateKey(), certificate.getPrivateKey());

            throw new RuntimeException("SOMEHOW THE PRIVATE KEYS ARE NOT EQUAL!");
        }

        LOG.infof("Persisting consumer data...");

        // persist
        this.consumerCurator.persist(kpdata);
        this.consumerCurator.persist(certificate);
        this.consumerCurator.persist(consumer);

        LOG.infof("Generating SCA Certificate for consumer...");
        this.getSCACertificate(org, consumer);
        LOG.infof("  done");

        LOG.infof("Converting consumer back to a CP consumer DTO...");

        // convert consumer to dto & return
        return this.convertToConsumerDTO(consumer);
    }

    private KeyPairData generateConsumerKeyPair(Consumer consumer) {
        KeyPair keypair = this.keyGenerator.generateKeyPair();

        KeyPairData kpdata = new KeyPairData()
            .setPublicKey(this.pkiUtil.toPemEncodedString(keypair.getPublic()))
            .setPrivateKey(this.pkiUtil.toPemEncodedString(keypair.getPrivate()));

        consumer.setKeyPair(kpdata);

        return kpdata;
    }

    private Certificate generateIdentityCert(Consumer consumer) {
        KeyPairData kpdata = consumer.getKeyPair();
        if (kpdata == null) {
            throw new IllegalStateException("consumer does not yet have a key pair");
        }

        KeyPair keypair = this.pkiUtil.convertToKeyPair(kpdata);

        Instant validAfter = Instant.now();
        Instant validUntil = validAfter.plus(15 * 365, ChronoUnit.DAYS);

        X509Certificate x509cert = this.certBuilderProvider.get()
            .generateCertificateSerial()
            .setDistinguishedName("CN=" + consumer.getOid())
            .setSubjectAltName(consumer.getName())
            .setValidAfter(validAfter)
            .setValidUntil(validUntil)
            .setKeyPair(keypair)
            .build();

        Certificate cert = new Certificate()
            .setSerialNumber(x509cert.getSerialNumber().longValue())
            .setValidAfter(validAfter)
            .setValidUntil(validUntil)
            .setPrivateKey(this.pkiUtil.toPemEncodedString(keypair.getPrivate()))
            .setCertificate(this.pkiUtil.toPemEncodedString(x509cert));

        consumer.setCertificate(cert);

        return cert;
    }

    private Consumer convertToConsumer(Organization org, CPConsumerDTO dto, String username) {
        CPConsumerTypeDTO ctype = dto.getType();
        String type = ctype != null ? ctype.getLabel() : null;

        Instant now = Instant.now();

        Consumer consumer = new Consumer()
            .setOid(UUID.randomUUID().toString())
            .setName(dto.getName())
            .setType(type != null ? type : "SYSTEM")
            .setOrganization(org)
            .setUsername(username)
            .setFacts(dto.getFacts())
            .setLastCheckIn(now)
            .setLastCloudProfileUpdate(now);

        return consumer;
    }

    private CPConsumerDTO convertToConsumerDTO(Consumer consumer) {
        Instant now = Instant.now();

        CPConsumerTypeDTO type = new CPConsumerTypeDTO()
            .setCreated(now)
            .setUpdated(now)
            .setId(consumer.getType())
            .setLabel(consumer.getType())
            .setManifest(false);

        Organization org = consumer.getOrganization();
        CPOwnerDTO owner = null;
        if (org != null) {
            owner = new CPOwnerDTO()
                .setId(org.getId())
                .setKey(org.getOid())
                .setDisplayName(org.getName())
                .setContentAccessMode("org_environment");
        }

        Certificate certificate = consumer.getCertificate();
        CPCertificateDTO identityCert = null;
        if (certificate != null) {
            CPCertificateSerialDTO certSerial = new CPCertificateSerialDTO()
                .setCreated(certificate.getCreated())
                .setUpdated(certificate.getCreated())
                .setId(certificate.getSerialNumber())
                .setSerial(certificate.getSerialNumber()) // pointless legacy junk
                .setExpiration(certificate.getValidUntil())
                .setRevoked(false);

            identityCert = new CPCertificateDTO()
                .setId(certificate.getId())
                .setCreated(certificate.getCreated())
                .setUpdated(certificate.getCreated())
                .setKey(certificate.getPrivateKey())
                .setCert(certificate.getCertificate())
                .setSerial(certSerial);
        }

        CPConsumerDTO consumerDTO = new CPConsumerDTO()
            .setId(consumer.getId())
            .setCreated(consumer.getCreated())
            .setUpdated(consumer.getUpdated())
            .setUuid(consumer.getOid())
            .setName(consumer.getName())
            .setOwner(owner)
            .setUsername(consumer.getUsername())
            .setFacts(consumer.getFacts())
            .setIdCert(identityCert)
            .setHref("/candlepin/consumers/" + consumer.getOid())
            .setActivationKeys(List.of())
            .setEnvironments(List.of());

        return consumerDTO;
    }


// {
//   "lastUpdate" : "2023-06-05T02:46:21+0000",
//   "contentListing" : {
//     "528232839942281128" : [ "
//         -----BEGIN CERTIFICATE-----
//         MIIGAzCCA+ugAwIBAgIIB1So+B90l6gwDQYJKoZIhvcNAQELBQAwRDEcMBoGA1UE
//         AwwTQ2FuZGxlcGluIFNlcnZlciBDQTESMBAGA1UECwwJQ2FuZGxlcGluMRAwDgYD
//         VQQKDAdSZWQgSGF0MB4XDTIzMDYwNTAxNDYyMVoXDTI0MDYwNTAxNDYyMVowTTEc
//         MBoGA1UECgwTdGVzdF9vd25lci1BMUYxNzNCNjEtMCsGA1UEAwwkOGQ3YWUyZWUt
//         YzJmYy00NDZiLWJiMTAtMTA1Nzg0OTVkYWI2MIICIjANBgkqhkiG9w0BAQEFAAOC
//         Ag8AMIICCgKCAgEA5mxCl78BY0fgjFBkvGunqK4CryFHYfgCc/UOZx1/iDwk/Ugb
//         69lhj1/jB3YERMp96+gqNQy9fUGtvG0hee6Pzqde1UtsEDY6eh+SlaXPugCqmxVv
//         F7FUJhpWVMwUahjdaYFvXrT0J4d6QXRSAiqVtsn/9hg1Y8QGzlocUPzLC9FL3HId
//         xWdo0nG7nwrLHqOEnwNpLO9Rlt4Nsl54J35pnenN1bzxWXnFMG6ie9bvNh3+b5+v
//         HKPJKFt14nsw8u/scVXP2Ps0qBNgiC06AUfppPqI+k1fbr2ZmGBVac6JlaKDSCac
//         Ns0/j755UUXcyNEkDuYL4XtiY5rvLYcfzj6cQQZwNtPNwqSJVevf5jFEQuv5EKi+
//         vZkp2iv+z6278uWh2rWA9U8XUo7C73NiNnd9eYiEe9n5pnxocHpDBJUBep2hUCPn
//         lbFIWzsjLIV1fODRGdtKqPP3XDGc15rchWSQ7VuV0+GN6OlpBgFGLZf3AANPsZPp
//         1YazovQpM09gW0NOPO3hKrUg6pGXadPdbvoLzuDYPHS1YLLn8cn8M8zOffD5oOVm
//         FF9hJJdnGTuZzn14P81FjoNMBWQWcrytesgTcHYY9ndmE11kcUllu2T8eJTN5TBe
//         Y3wqIwNG11msVgMPaiB+HgdqJMCwJtEpt12sp+jfY1+vfPJbAdaoVWpI2gsCAwEA
//         AaOB7zCB7DAOBgNVHQ8BAf8EBAMCBLAwEwYDVR0lBAwwCgYIKwYBBQUHAwIwCQYD
//         VR0TBAIwADARBglghkgBhvhCAQEEBAMCBaAwHQYDVR0OBBYEFE4qJlxMCCDKcG7f
//         DvRFycTAG7/oMB8GA1UdIwQYMBaAFPfWFAIdJ+M3s3PaGri9qw9V+NyCMBIGCSsG
//         AQQBkggJBgQFDAMzLjQwFwYJKwYBBAGSCAkIBAoMCE9yZ0xldmVsMDoGCSsGAQQB
//         kggJBwQtBCt42itJLS6Jzy/PSy3SdTR0MzQ3djJjyEvMTdW1BLLMHd0MGADGEAnj
//         A6YAMA0GCSqGSIb3DQEBCwUAA4ICAQByjm/90LtxHrtXbwzd4NvnwMZ/CPBQaZEJ
//         dJWkDFa7PuxC76sJ1jHeX4h63nl2Y8WOH0S38Dr9zRzy4vca1R/cO31xU3GVDyi4
//         +1hk+sKldQVPUWmhGiNCMZN0I7062uz4pE3bEYGshTPTEyJlQPyB2NHXyuTY1mH/
//         7/55hh8ZEnw4OWXT2QNlFLn81nw9L/NWT5fY5kdSglX19f5X8xxnN7X18xCcpG9i
//         PGOaPnAAD0zzjlYNUS0KR9WrRIWEgUDTZ/L39GW1hWQbLwF7cVWvSIrJdxbOq9w6
//         bNIc09kQoTsMSbkKtYZ4lgRI0MpUarqpcW2G+vFe84d9PSPzV+jvW9c1jdxahFRT
//         Rk8KQBN0PoX/HryM7KG/v0W8EtkTJ844qginVfTXUwA9RJCdKfOMadkWTjACBV6/
//         wfv5O2RNOQTgyLEvVuieOLn0IEb6CVuMZUlEupAVpr8x+pJJPu2kGZ7GqB40PA9A
//         ZwDmlZpalAh7tWcDywb1pg8f/3YM8N00eaeaAA4jMNSphwLeVjwZ1p6k3eu6VRQi
//         TmeQO/T+hh/DyWO8wIhFJPtretZTyqsAykLL2+IKLNVN2MvMW1f0Yqatn0uAWoIJ
//         DbjP6pa0+3b1dhD6Gq9EsgPxlZt0SqTlHmaNojajTXGiM8oiXzJbkBXrmq/Wq2DJ
//         XSJRBDGHmA==
//         -----END CERTIFICATE-----
//     ", "
//         -----BEGIN ENTITLEMENT DATA-----
//         eJx9js1qwzAQhF/F7NkCWbUdV7fQV8gppQRptSGmiWT004vRu3ed0EshOc7szLez
//         Agafyo0iaJjczpAiEqjOKPp+tMLaTopODrupfx+csSO0kIpNGOclz8GDXiF9Fy4z
//         J5PPJ4NIKXHMmxux//Hwm/3Dry2E6LZ3XMwmZo4oqd6EHIUcDlLpftSqOzKAvHt2
//         ZMoSgyuYE+jPFWb3YkHzb0ILPxTTfTywMBEvcybMJdJG+2r/SJuoLJcQrry31l8G
//         qF5y
//         -----END ENTITLEMENT DATA-----
//         -----BEGIN RSA SIGNATURE-----
//         KAeeXAhspiRBSeJ+ENOiNfBoqdCQb01YFgkPJEpAo7mtQFE5vxjjeBjOW704yQBF
//         arKRmexj5Of/nRI2TLiWtFFWAQQNVaZgZyfKOKa23OoqObyANYml7QbkGJHL99Oo
//         zs4di1UO73RgyDxocpfLBcUwRs19rd6EuZqx+towxnKn9cyuTH72iFVG2VDLesgm
//         pcKviSFY7ohN37X5BkzGgDsn21tTLal0s/XewsgXku19jOPCi2WWT5aGbjBdqjXa
//         Tq7czmUxxgxUPl6fHZx17dFzsLcF/9Cs93ClT0gstWmpjpdCz8GtXYfRZDLhl/il
//         FTJdMWh64bEMOGX80ohy3aRfM9/85g5urBIx+sRzAyOFlOBZgBdrZjnQtofPLWiy
//         pK73QRf+BoIVX2RmGrlEVJpkxsZkEpbTk4gExhl09TJ59Y+htxGH08zirD+l0UEq
//         g3YIkidG3/r7/ki/2enuiYtZZBirMr9tWicJu/fLkC2Xoq/qlnQfHNijwsuQUBMF
//         ql7R2S+GpAfLZ3PFCmCWBt1TM9DBprKLExCXGGQOf3ySOp6Q2bZBcPSGJF968qST
//         YjM+HkFXBarVCxiyxgXCftC4uuKaYyXMW8IT8Kgz+mPLL5eWjfcoy+CgTqOz3ct2
//         aoKrpNeYrqKNXhT9I6Hh6aRgKe6BvOBNQarugpnPy60=
//         -----END RSA SIGNATURE-----
//     " ]
//   }
// }

    @GET
    @Path("/{consumer_oid}/accessible_content")
    @Produces(MediaType.APPLICATION_JSON)
    public CPContentAccessListing getAccessibleContent(
        @PathParam("consumer_oid") String consumerOid,
        @QueryParam("last_update") Instant lastUpdate,
        @QueryParam("arch") List<String> archFilter) {

        Consumer consumer = this.consumerCurator.getConsumerByOid(consumerOid);
        if (consumer == null) {
            LOG.errorf("no such consumer: %s", consumerOid);
            throw new NotFoundException("No such consumer: " + consumerOid);
        }

        Organization org = consumer.getOrganization();
        LOG.infof("Using organization: %s", org);

        SCAContentCertificate scaCert = this.getSCACertificate(org, consumer);

        // This is profoundly silly. We rip apart a valid PEM-formatted cert + payload to turn it
        // into bad JSON!? WHY WHY WHY!?
        List<String> pieces = List.of(scaCert.getCertificate(), scaCert.getContentData());

        CPContentAccessListing output = new CPContentAccessListing()
            .setContentListing(scaCert.getSerialNumber(), pieces)
            .setLastUpdate(scaCert.getUpdated());

        return output;
    }

    private SCAContentCertificate getSCACertificate(Organization org, Consumer consumer) {
        SCACertificateGenerator scaCertGen = this.scaCertGeneratorProvider.get();

        String filter = scaCertGen.buildSCAFilterString(org, consumer);

        SCAContentCertificate scaCert = this.scaCertCurator.getSCACertificateByFilter(filter);
        if (scaCert != null) {
            return scaCert;
        }

        scaCert = scaCertGen.generateSCACertificate(org, consumer);
        this.scaCertCurator.persist(scaCert);

        return scaCert;
    }

}
