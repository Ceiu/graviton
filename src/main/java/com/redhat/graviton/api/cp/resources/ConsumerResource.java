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
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.MediaType;

import java.time.Instant;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.DeflaterOutputStream;
import java.util.stream.*;
import java.security.Signature;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;


// hard-coded consumer identity cert for demo purposes
// -----BEGIN CERTIFICATE-----
// MIIGHTCCBAWgAwIBAgIIUVcKGDroW2wwDQYJKoZIhvcNAQELBQAwRDEcMBoGA1UE
// AwwTQ2FuZGxlcGluIFNlcnZlciBDQTESMBAGA1UECwwJQ2FuZGxlcGluMRAwDgYD
// VQQKDAdSZWQgSGF0MB4XDTIzMDYwNTAxNDYxNloXDTI4MDYwNTAyNDYxNlowTTEc
// MBoGA1UECgwTdGVzdF9vd25lci1BMUYxNzNCNjEtMCsGA1UEAwwkOGQ3YWUyZWUt
// YzJmYy00NDZiLWJiMTAtMTA1Nzg0OTVkYWI2MIICIjANBgkqhkiG9w0BAQEFAAOC
// Ag8AMIICCgKCAgEA5mxCl78BY0fgjFBkvGunqK4CryFHYfgCc/UOZx1/iDwk/Ugb
// 69lhj1/jB3YERMp96+gqNQy9fUGtvG0hee6Pzqde1UtsEDY6eh+SlaXPugCqmxVv
// F7FUJhpWVMwUahjdaYFvXrT0J4d6QXRSAiqVtsn/9hg1Y8QGzlocUPzLC9FL3HId
// xWdo0nG7nwrLHqOEnwNpLO9Rlt4Nsl54J35pnenN1bzxWXnFMG6ie9bvNh3+b5+v
// HKPJKFt14nsw8u/scVXP2Ps0qBNgiC06AUfppPqI+k1fbr2ZmGBVac6JlaKDSCac
// Ns0/j755UUXcyNEkDuYL4XtiY5rvLYcfzj6cQQZwNtPNwqSJVevf5jFEQuv5EKi+
// vZkp2iv+z6278uWh2rWA9U8XUo7C73NiNnd9eYiEe9n5pnxocHpDBJUBep2hUCPn
// lbFIWzsjLIV1fODRGdtKqPP3XDGc15rchWSQ7VuV0+GN6OlpBgFGLZf3AANPsZPp
// 1YazovQpM09gW0NOPO3hKrUg6pGXadPdbvoLzuDYPHS1YLLn8cn8M8zOffD5oOVm
// FF9hJJdnGTuZzn14P81FjoNMBWQWcrytesgTcHYY9ndmE11kcUllu2T8eJTN5TBe
// Y3wqIwNG11msVgMPaiB+HgdqJMCwJtEpt12sp+jfY1+vfPJbAdaoVWpI2gsCAwEA
// AaOCAQgwggEEMA4GA1UdDwEB/wQEAwIEsDATBgNVHSUEDDAKBggrBgEFBQcDAjAJ
// BgNVHRMEAjAAMBEGCWCGSAGG+EIBAQQEAwIFoDAdBgNVHQ4EFgQUTiomXEwIIMpw
// bt8O9EXJxMAbv+gwHwYDVR0jBBgwFoAU99YUAh0n4zezc9oauL2rD1X43IIwfwYD
// VR0RBHgwdqRPME0xHDAaBgNVBAoME3Rlc3Rfb3duZXItQTFGMTczQjYxLTArBgNV
// BAMMJDhkN2FlMmVlLWMyZmMtNDQ2Yi1iYjEwLTEwNTc4NDk1ZGFiNqQjMCExHzAd
// BgNVBAMMFnRlc3RfY29uc3VtZXItMzY3MDM1REEwDQYJKoZIhvcNAQELBQADggIB
// AFMRo72gEr6coUmLowOnmkxSZoFROdMJ4Hy9ip2JcCKK4gBZ4wuGKV9+oUhGL/UZ
// h1vSzn3XhfJav0auPSn79zVybGuMnzwMr7IqB/SsNv1vLERc96i37dLWFVZCqHG+
// 0VZP4x+5seQIKktGZhGB/TApeOnAl9P8hikpYSU7c6PbHZ/2PYSyNbeglA5p6u0Z
// JX62OZaZ3zu1jh8gbx6+2DpK4abES1DPMWtwbKN6k3Y1OdZg6n2STpczmeim2N5i
// 6CTp+PQ9gL4D5IcNFdVE54XRfzkp8ngB7lLqpHhqBbssBXAPgA6eDBJQ33MSL4I0
// LOOeCEnWb6jV+MbP95X3JaR8i8tfH/uROiREOpxeDsWaf7dppce4aaJMZZxS9OXH
// Yc9imX0lAs6E4Ed7nIahbcLnJloyi4SEUZ0oyxyT2GqFaDwSIGEGv2hfbvgfYZRf
// cxoZvhGZjCYMDy+9Xn+zImkthSWzNErMZRqf8W+QeEy1EVr8q7hLyDftQgNu8B5l
// 7/plHQE5A6axKxMIjecUtHX8b0SKKneWbICT2jwKfV78r7VsyPSKW/H0UPILzNmR
// PQAM4gHC0hVj90LkUb/zpVRRLgT+M9ZdHr57jkoxRb96voqYWE1oawaYWCzGDzfC
// BXvpIDgNHWOb5bJQuDZ8d3zqF6boG0ZRpZHq9flHAn2X
// -----END CERTIFICATE-----

// hard-coded consumer private key for demo purposes
// -----BEGIN PRIVATE KEY-----
// MIIJQwIBADANBgkqhkiG9w0BAQEFAASCCS0wggkpAgEAAoICAQDmbEKXvwFjR+CM
// UGS8a6eorgKvIUdh+AJz9Q5nHX+IPCT9SBvr2WGPX+MHdgREyn3r6Co1DL19Qa28
// bSF57o/Op17VS2wQNjp6H5KVpc+6AKqbFW8XsVQmGlZUzBRqGN1pgW9etPQnh3pB
// dFICKpW2yf/2GDVjxAbOWhxQ/MsL0Uvcch3FZ2jScbufCsseo4SfA2ks71GW3g2y
// Xngnfmmd6c3VvPFZecUwbqJ71u82Hf5vn68co8koW3XiezDy7+xxVc/Y+zSoE2CI
// LToBR+mk+oj6TV9uvZmYYFVpzomVooNIJpw2zT+PvnlRRdzI0SQO5gvhe2Jjmu8t
// hx/OPpxBBnA2083CpIlV69/mMURC6/kQqL69mSnaK/7Prbvy5aHatYD1TxdSjsLv
// c2I2d315iIR72fmmfGhwekMElQF6naFQI+eVsUhbOyMshXV84NEZ20qo8/dcMZzX
// mtyFZJDtW5XT4Y3o6WkGAUYtl/cAA0+xk+nVhrOi9CkzT2BbQ0487eEqtSDqkZdp
// 091u+gvO4Ng8dLVgsufxyfwzzM598Pmg5WYUX2Ekl2cZO5nOfXg/zUWOg0wFZBZy
// vK16yBNwdhj2d2YTXWRxSWW7ZPx4lM3lMF5jfCojA0bXWaxWAw9qIH4eB2okwLAm
// 0Sm3Xayn6N9jX6988lsB1qhVakjaCwIDAQABAoICADMtDTc9rVW7A4S2YEE22cRN
// bXwu9HcuSYEuhpXhR89eqEp2pvjhTVk2Dyq7i+QpYwEvjZw9qaI1oZwNUteNY/Qe
// WSIfP690E9XGyFwjOPkTJ9EM2CEXedyCkfUAZHJR9m711+HdZ7KV1NHPv10KzziL
// MaWbJ9RyF57iBZL5KjGEOQiLcdBDkNTYfLasg4NZBt34OHZJ6sGKrNkWQTJoUZXI
// dTZI4gOg12pT0BOYH4XR8I4jFZXTTJyBf7Oe77atEcL0L5b95+qCbzifbtgU/C/v
// ktegspbaVlAw6Rd4TzdkIis2pBBJ0pme0Lv7u7cAODV042OfbzE3GnLupWI9pp8g
// nqf+imf9k8egE/Aj77UsC+hwf5CayWMciZkBRt+LGuDgC1pR2tmNgmWUA0uAUJfN
// PDHweHq1eYUuRHAKfzD8qBeZvW5kYIsahfPQEYuV+PdJVEXxnPgD2bpG5eniQ/dY
// 8RNgRCWYhEJWFT5cCSzlsfW70pybz0CdKmkisFMkh6M/vtMvrKHlkVhanP22tTOe
// axOtRqGW4/YUp5tMUX8+IxzblWpTqsMqk32u/Q17iy8935B8hcQxeosMax9J8mWz
// JqD/DSQq/kebnCZ5pNR59V72/P0vM0tmXgFkVszRMQ8+I/tzZ5zMnXT+sddKMfr8
// dksM8fHGKYcfnnKYhZR1AoIBAQD5QeliXzIpRuZIFQhMkKOqxc7sLAfS8aZBvafb
// mjGnHDcOT4R+fhNMj7DyD0bNdgW+H1wJWviYRXaTrRtmIFjK8TYaLtQGQXOnhWfu
// 5W2scW5IBkBqNVoEBPHbIqziJQdt6ely1eQ/Mf0xUh3bOP9+8KHy8GDZgXczptF5
// YD5zzB+cRYAe3k8xem4kFVw3N2iBQOjKxHTYmpS2EXvfbmijVnllsDbiK3lBs6tq
// T1+NFvJcuQRPCUG2IXJu9tXKm7WTxhr0sFfLN/nBSSVQKqyfPAtoxRzOdXXy5g1b
// tSOSBzw0BdkRf4RdMmBTCYrzyBc8DhmNsvgTioG02bzbWy8VAoIBAQDsp+umss4B
// C6n48WOds8CJBND5RwlYeLMBlwTJvF0yM/8ugFMYtLHPjfy16fQ8LrlTw9cxFzSm
// bPTtVlWLBKIsS+8yaSAMQPZiacx3lY/KAF/qeb0cfq4RPwqX4Jh6TGw2mdaxYprx
// AjY+mN6VRkGcq+UULXbZm5XF5AiO6TmT/GyiHXvG4o70dCZhw7ksf75qhL5athDf
// 8m9U+tasEQHUxrDNxPCGyN+t1vOt1pmqvzCzOBphCPeGMg6TqEfF9Qg3jrKSWhQb
// GteUtXVYXDzFfvrz7gLmgZBgFoCOhE04HGyV8UEuY9aiZgeiqtYeQqOuBHOCETmQ
// 69y4gd16EyyfAoIBAAquAEnuuj96gjNkc06Ug+61OwBzgoJPSEhYz/Bg0u/ODa3X
// Sl4BGOKW5YX5kZcMD4Lt5QuhkjU0BLifGiVuDV/kpeTJTNxXwZTT6ngZ7HEPCyKa
// 4ferAYhajeJNwgqOe5sCArag436xrZ+HXTAfaRMPTGEUW5H2Mh1McH062nIrtM8F
// S1AHRm1gSYfSQnk7LocbON43PKprNSP3687O9DzLX7gzrKZUJoXiJN4ROI7bBNdk
// NgMM6nvbQwPuH4rm0Qjb3BvP0nMqmtDlSWv96BeKgxKiH3HZJoyZvsjHIhcHqFSy
// S3KsfAdiOS5VolZ/pD82e3HO0UJFFPmiuqzLY2ECggEBAK46fjJO5307khqV1KoQ
// 4Um/Av7S/p1k1xzvivXeuJoyT8dzcDn7JgFRXmCrakU4fPafcvlQRVHVW4i0C+x6
// L87+5I4veQmsdfhEPeU4dhDYr8TXdZPZ69sEl47zKi63vt9/6ODVYI7Y+wig0RYD
// EGEA6Hkvc5WHhv+W/3n/WIWLzIqZvDvTJj3wmSVWzChnr8+KBP6RleDaAn8E4TDy
// oG9/DEGhoRcKIitA2kv8d0uO0JLRVhJkYJ8qkTLCLH0gBbmpU+yNZsoBu/9ejJLN
// 7WvVLXqSmw4LCzjCuEi0PIl327WUVVGK7UTIawymS1ch5pFB86wpdEEudGReL6kl
// Hq8CggEBAIJMAjJ0kHJyE5uHcVFwlo2GDPx2+/xAx7IAXYXBFV7cExKrdCS4tuib
// 41iwNJrzev5OS1DBf0VXjFMBRDIQUs10nlJr/CHRDMwcos0gAwIzUMO6V5DL53ej
// XJDP/r23UoEQM4Y+uihy+5tETtD4earr3MFOsJJW3GmbfDOh97Rxihfk61NVjVsu
// 7tzLlyZtZ6JjawVTfqazKny1J34J7e0THO2Xfbp4qWQspcdCM4Ets0sBiPwNthip
// wvletpSmF5KX44Ds2vRi5gkD8Zu98CnyAVipISUdcTH6hcVNXWgBx4i52RRHp1jn
// 7geC9snQyracsdxJ+g91ahVd6XaWK+M=
// -----END PRIVATE KEY-----





@Path("/candlepin/consumers")
public class ConsumerResource {
    private static final Logger LOG = Logger.getLogger(ConsumerResource.class);

    @Inject
    private ObjectMapper mapper;

    @Inject
    private OrganizationCurator orgCurator;

    @Inject
    private ProductCurator productCurator;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public CPConsumerDTO register() {
        return new CPConsumerDTO().setValue("consumer " + Math.random());
    }

    // /consumers/{consumer_uuid}/accessible_content:
    // get:
    //   description: Retrieves the body of the Content Access Certificate for the Consumer
    //   tags:
    //     - consumer
    //   operationId: getContentAccessBody
    //   security: [ ]
    //   x-java-response:
    //     type: javax.ws.rs.core.Response
    //     isContainer: false
    //   parameters:
    //     - name: consumer_uuid
    //       in: path
    //       description: The UUID of the consumer
    //       required: true
    //       schema:
    //         type: string
    //     - name: If-Modified-Since
    //       in: header
    //       description: Modified date. Accepted format EEE, dd MMM yyyy HH:mm:ss z
    //       schema:
    //         type: string

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
    @Path("/{consumer_uuid}/accessible_content")
    @Produces(MediaType.APPLICATION_JSON)
    public CPContentAccessListing getContentAccessBody(
        @PathParam("consumer_uuid") String consumerUuid,
        @QueryParam("last_update") Instant lastUpdate,
        @QueryParam("arch") List<String> archFilter) {

        Consumer consumer = new Consumer()
            .setId(consumerUuid);

        // For now, use the consumer UUID as the org ID. They're effectively the same in this context anyway
        Organization org = this.orgCurator.getOrgById(consumerUuid);
        LOG.infof("Using organization: %s", org);
        if (org == null) {
            // TODO: Actually add an exception mapper for this
            LOG.errorf("Org not found: %s", consumerUuid);
            throw new NotFoundException("org not found: " + consumerUuid);
        }

        Map<String, Content> contentMap = this.productCurator.getOrgContent(org.getOid(), true);
        this.filterContent(contentMap, archFilter);

        Instant lastUpdated = Instant.now();
        Long certSerial = (long) (Math.random() * Long.MAX_VALUE);

        // This is profoundly silly. We rip apart a valid PEM-formatted cert + payload to turn it
        // into bad JSON!? WHY WHY WHY!?
        List<String> pieces = List.of(
            this.getCACertData(org, null, contentMap),
            this.getCAEntitlementData(org, consumer, contentMap)
        );

        CPContentAccessListing output = new CPContentAccessListing()
            .setContentListing(certSerial, pieces)
            .setLastUpdate(lastUpdated);

        return output;
    }

    // Arch filtering junk
    private static final Set<String> GLOBAL_ARCHES = Set.of("all", "ALL", "noarch");
    private static final Set<String> X86_LABELS = Set.of("i386", "i486", "i586", "i686");

    private void filterContent(Map<String, Content> contentMap, Collection<String> archFilter) {
        if (archFilter == null || archFilter.isEmpty()) {
            return;
        }

        Iterator<Map.Entry<String, Content>> contentIterator = contentMap.entrySet().iterator();
        while (contentIterator.hasNext()) {
            Map.Entry<String, Content> entry = contentIterator.next();

            if (!this.archesMatch(archFilter, entry.getValue())) {
                contentIterator.remove();
            }
        }
    }

    private Set<String> parseArches(String arches) {
        Set<String> parsed = new HashSet<>();

        if (arches == null || arches.trim().isEmpty()) {
            return parsed;
        }

        for (String arch : arches.trim().split("\\s*,[\\s,]*")) {
            parsed.add(arch);
        }

        return parsed;
    }

    private boolean archesMatch(Collection<String> archFilter, Content content) {
        Set<String> contentArches = this.parseArches(content.getArches());

        // No arch specified, auto-match?
        if (contentArches.isEmpty()) {
            return true;
        }

        // Check the arch filter for any matches
        for (String contentArch : contentArches) {
            if (GLOBAL_ARCHES.contains(contentArch)) {
                return true;
            }

            for (String arch : archFilter) {
                if (arch.equals(contentArch)) {
                    return true;
                }

                if (X86_LABELS.contains(arch) && contentArch.equals("x86")) {
                    return true;
                }
            }
        }

        return false;
    }

    // cert generation junk
    private String getCACertData(Organization org, Object consumer, Map<String, Content> contentMap) {
        // Hard coded cert for demo purposes
        return "-----BEGIN CERTIFICATE-----\n" +
            "MIIGAzCCA+ugAwIBAgIIB1So+B90l6gwDQYJKoZIhvcNAQELBQAwRDEcMBoGA1UE\n" +
            "AwwTQ2FuZGxlcGluIFNlcnZlciBDQTESMBAGA1UECwwJQ2FuZGxlcGluMRAwDgYD\n" +
            "VQQKDAdSZWQgSGF0MB4XDTIzMDYwNTAxNDYyMVoXDTI0MDYwNTAxNDYyMVowTTEc\n" +
            "MBoGA1UECgwTdGVzdF9vd25lci1BMUYxNzNCNjEtMCsGA1UEAwwkOGQ3YWUyZWUt\n" +
            "YzJmYy00NDZiLWJiMTAtMTA1Nzg0OTVkYWI2MIICIjANBgkqhkiG9w0BAQEFAAOC\n" +
            "Ag8AMIICCgKCAgEA5mxCl78BY0fgjFBkvGunqK4CryFHYfgCc/UOZx1/iDwk/Ugb\n" +
            "69lhj1/jB3YERMp96+gqNQy9fUGtvG0hee6Pzqde1UtsEDY6eh+SlaXPugCqmxVv\n" +
            "F7FUJhpWVMwUahjdaYFvXrT0J4d6QXRSAiqVtsn/9hg1Y8QGzlocUPzLC9FL3HId\n" +
            "xWdo0nG7nwrLHqOEnwNpLO9Rlt4Nsl54J35pnenN1bzxWXnFMG6ie9bvNh3+b5+v\n" +
            "HKPJKFt14nsw8u/scVXP2Ps0qBNgiC06AUfppPqI+k1fbr2ZmGBVac6JlaKDSCac\n" +
            "Ns0/j755UUXcyNEkDuYL4XtiY5rvLYcfzj6cQQZwNtPNwqSJVevf5jFEQuv5EKi+\n" +
            "vZkp2iv+z6278uWh2rWA9U8XUo7C73NiNnd9eYiEe9n5pnxocHpDBJUBep2hUCPn\n" +
            "lbFIWzsjLIV1fODRGdtKqPP3XDGc15rchWSQ7VuV0+GN6OlpBgFGLZf3AANPsZPp\n" +
            "1YazovQpM09gW0NOPO3hKrUg6pGXadPdbvoLzuDYPHS1YLLn8cn8M8zOffD5oOVm\n" +
            "FF9hJJdnGTuZzn14P81FjoNMBWQWcrytesgTcHYY9ndmE11kcUllu2T8eJTN5TBe\n" +
            "Y3wqIwNG11msVgMPaiB+HgdqJMCwJtEpt12sp+jfY1+vfPJbAdaoVWpI2gsCAwEA\n" +
            "AaOB7zCB7DAOBgNVHQ8BAf8EBAMCBLAwEwYDVR0lBAwwCgYIKwYBBQUHAwIwCQYD\n" +
            "VR0TBAIwADARBglghkgBhvhCAQEEBAMCBaAwHQYDVR0OBBYEFE4qJlxMCCDKcG7f\n" +
            "DvRFycTAG7/oMB8GA1UdIwQYMBaAFPfWFAIdJ+M3s3PaGri9qw9V+NyCMBIGCSsG\n" +
            "AQQBkggJBgQFDAMzLjQwFwYJKwYBBAGSCAkIBAoMCE9yZ0xldmVsMDoGCSsGAQQB\n" +
            "kggJBwQtBCt42itJLS6Jzy/PSy3SdTR0MzQ3djJjyEvMTdW1BLLMHd0MGADGEAnj\n" +
            "A6YAMA0GCSqGSIb3DQEBCwUAA4ICAQByjm/90LtxHrtXbwzd4NvnwMZ/CPBQaZEJ\n" +
            "dJWkDFa7PuxC76sJ1jHeX4h63nl2Y8WOH0S38Dr9zRzy4vca1R/cO31xU3GVDyi4\n" +
            "+1hk+sKldQVPUWmhGiNCMZN0I7062uz4pE3bEYGshTPTEyJlQPyB2NHXyuTY1mH/\n" +
            "7/55hh8ZEnw4OWXT2QNlFLn81nw9L/NWT5fY5kdSglX19f5X8xxnN7X18xCcpG9i\n" +
            "PGOaPnAAD0zzjlYNUS0KR9WrRIWEgUDTZ/L39GW1hWQbLwF7cVWvSIrJdxbOq9w6\n" +
            "bNIc09kQoTsMSbkKtYZ4lgRI0MpUarqpcW2G+vFe84d9PSPzV+jvW9c1jdxahFRT\n" +
            "Rk8KQBN0PoX/HryM7KG/v0W8EtkTJ844qginVfTXUwA9RJCdKfOMadkWTjACBV6/\n" +
            "wfv5O2RNOQTgyLEvVuieOLn0IEb6CVuMZUlEupAVpr8x+pJJPu2kGZ7GqB40PA9A\n" +
            "ZwDmlZpalAh7tWcDywb1pg8f/3YM8N00eaeaAA4jMNSphwLeVjwZ1p6k3eu6VRQi\n" +
            "TmeQO/T+hh/DyWO8wIhFJPtretZTyqsAykLL2+IKLNVN2MvMW1f0Yqatn0uAWoIJ\n" +
            "DbjP6pa0+3b1dhD6Gq9EsgPxlZt0SqTlHmaNojajTXGiM8oiXzJbkBXrmq/Wq2DJ\n" +
            "XSJRBDGHmA==\n" +
            "-----END CERTIFICATE-----\n";
    }

    private String getCAEntitlementData(Organization org, Consumer consumer, Map<String, Content> contentMap) {
        try {
            // Convert to cert
            CertProduct certProduct = this.buildCertProduct(contentMap);
            CertEntitlement certEntitlement = this.buildCertEntitlement(consumer, certProduct);

            String json = this.mapper.writeValueAsString(certEntitlement);
            String b64compressed = this.compressAndEncodeJson(json);

            // base64 encode the payload and return it as a string
            return b64compressed;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private CertProduct buildCertProduct(Map<String, Content> contentMap) {

        // When it comes to filtering content, there are a lot of bits CP does that are
        // implicitly ignored by way of the product pass into the X509ExtensionUtil being a
        // minimialist product. This is probably not good (tm) long term, but makes life easier
        // for me here.
        List<CertContent> certContent = contentMap.values()
            .stream()
            .map(content -> this.convertToCertContent(content))
            .collect(Collectors.toList());

        CertProduct certProduct = new CertProduct()
            .setId("simple_content_access")
            .setName("Simple Content Access")
            .setContent(certContent)

            // sca does not define version
            .setVersion("")

            // sca does not define product branding
            .setBrandName(null)
            .setBrandType(null)

            // Amusingly, SCA cert payloads don't define arches, even though they're filtered by
            // arch. We'll leave it null for now but this shouldn't be left empty in the future.
            // We can do better, and we should.
            .setArchitectures(List.of());

        return certProduct;
    }

    private CertContent convertToCertContent(Content content) {
        CertContent certContent = new CertContent()
            .setId(content.getOid())
            .setType(content.getType())
            .setName(content.getName())
            .setLabel(content.getLabel())
            .setVendor(content.getVendor())
            .setGpgUrl(content.getGpgUrl())
            .setMetadataExpiration(content.getMetadataExpiration())

            // In CP proper, this actually goes through a bunch of checks, but for SCA certs, the
            // sku product has no attributes and, thus, no content enablement overrides. Since we
            // also don't have environments to deal with, this means we just copy it over 1:1.
            .setEnabled(content.getEnabled());

        // Include required tags as a list if present...
        String requiredTags = content.getRequiredTags();
        if (requiredTags != null) {
            List<String> parsedTags = List.of(requiredTags.trim().split("\\s*,[\\s,]*"));
            certContent.setRequiredTags(parsedTags);
        }

        return certContent;
    }

    private CertEntitlement buildCertEntitlement(Consumer consumer, CertProduct certProduct) {
        CertEntitlement certEntitlement = new CertEntitlement()
            .setConsumer(consumer.getId())
            .setProducts(List.of(certProduct))

            // SCA certs don't specify a quantity
            .setQuantity(null)

            // When building cert objects for SCA certs, CP uses a largely unpopulated dummy pool.
            // This means much of it will be nulled or empty population and there's no real value
            // in attempting to pass something through.
            .setSubscription(this.buildCertSubscription(certProduct))
            .setOrder(this.buildCertOrder())
            .setPool(this.buildCertPool(certProduct));

        return certEntitlement;
    }

    private CertSubscription buildCertSubscription(CertProduct certProduct) {
        CertSubscription certSubscription = new CertSubscription()
            .setSku(certProduct.getId())
            .setName(certProduct.getName());

        // The following properties never get set for an SCA cert, as the product has no attributes:
        // WARNING_PERIOD
        // SOCKETS
        // RAM
        // CORES
        // MANAGEMENT_ENABLED
        // STACKING_ID
        // VIRT_ONLY
        // USAGE
        // ROLES
        // ADDONS
        // SUPPORT_LEVEL
        // SUPPORT_TYPE

        return certSubscription;
    }

    private CertOrder buildCertOrder() {
        CertOrder certOrder = new CertOrder();

        // The following properties are never set for an SCA cert:
        // number
        // quantity
        // account number
        // contract number

        // The dates *are* set, but both the start *AND* end date are set to now...
        Instant start = Instant.now();
        Instant end = start; // start.plus(1, ChronoUnit.YEARS);

        // ...but they're set as strings. AAAAAARGH. Luckily it's an 8601 timestamp string, so this
        // is low-effort for us.
        certOrder.setStart(start.toString())
            .setEnd(end.toString());

        return certOrder;
    }

    private CertPool buildCertPool(CertProduct certProduct) {
        // We don't have a pool at all here, so we'll just steal the ID from the product. It's all
        // arbitrary anyway, so whatever I guess...

        CertPool certPool = new CertPool()
            .setId(certProduct.getId());

        return certPool;
    }

    private String compressAndEncodeJson(String json) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DeflaterOutputStream dos = new DeflaterOutputStream(baos);

        dos.write(json.getBytes(StandardCharsets.UTF_8));
        dos.finish();
        dos.close();

        byte[] compressed = baos.toByteArray();

        String b64encoded = Base64.getEncoder().encodeToString(compressed);
        byte[] signed = this.sign(compressed);

        return new StringBuilder()
            .append("-----BEGIN ENTITLEMENT DATA-----\n")
            .append(this.base64EncodeWithLineLimit(compressed, 64))
            .append("-----END ENTITLEMENT DATA-----\n")
            .append("-----BEGIN RSA SIGNATURE-----\n")
            .append(this.base64EncodeWithLineLimit(signed, 64))
            .append("-----END RSA SIGNATURE-----\n")
            .toString();




        // Convert the blob to b64 junk

        // String payload = "-----BEGIN ENTITLEMENT DATA-----\n";
        // payload += Util.toBase64(payloadBytes);
        // payload += "-----END ENTITLEMENT DATA-----\n";

        // byte[] bytes = pki.getSHA256WithRSAHash(new ByteArrayInputStream(payloadBytes));
        // String signature = "-----BEGIN RSA SIGNATURE-----\n";
        // signature += Util.toBase64(bytes);
        // signature += "-----END RSA SIGNATURE-----\n";
        // return payload + signature;

        // return b64encoded;
    }

    private String base64EncodeWithLineLimit(byte[] bytes, int lineLen) {

        String encoded = Base64.getEncoder().encodeToString(bytes);
        int length = encoded.length();

        StringBuilder builder = new StringBuilder();

        int offset = 0;
        while (offset + lineLen < length) {
            builder.append(encoded.substring(offset, offset + lineLen))
                .append('\n');

            offset += lineLen;
        }

        builder.append(encoded.substring(offset))
            .append('\n');

        return builder.toString();
    }

    private byte[] sign(byte[] bytesToSign) {
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(this.readCAPrivateKey());

            signature.update(bytesToSign, 0, bytesToSign.length);
            return signature.sign();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private X509Certificate loadCACert() {
        String path = "/home/crog/devel/graviton/candlepin-ca.crt";

        try (InputStream istream = new FileInputStream(path)) {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            return (X509Certificate) certFactory.generateCertificate(istream);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Stolen from google. Do not use blindly.
    private PrivateKey readCAPrivateKey() throws Exception {
        String path = "/home/crog/devel/graviton/candlepin-ca.key";

        String key = Files.readString(new File(path).toPath());

        String pem = key.replace("-----BEGIN PRIVATE KEY-----", "")
          .replaceAll(System.lineSeparator(), "")
          .replace("-----END PRIVATE KEY-----", "");

        byte[] encoded = Base64.getDecoder().decode(pem);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(encoded));
    }
}

// M 01001101
// a 01100001
// n 01101110

// 010011010110000101101110
// 010011010110000101101110
// 010011 010110 000100


// 0100110101100001
// 010011010110000100

// 010011 010110 000100

// Man => TWFu
// Ma  => TWE=
// M   => TQ==

