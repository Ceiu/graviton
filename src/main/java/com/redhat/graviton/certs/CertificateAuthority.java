package com.redhat.graviton.certs;


import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.DeflaterOutputStream;
import java.util.stream.*;
import java.security.Signature;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Provider;



// TODO:
// This should become an interface, and have different implementations depending on how/where we
// wish to load the certificate in question


@ApplicationScoped
public class CertificateAuthority {

    // TODO: change this to a configuration path
    private static final String CA_CERT_PATH = "/home/crog/devel/graviton/graviton-ca.crt";
    private static final String CA_PRIVATE_KEY_PATH = "/home/crog/devel/graviton/graviton-ca.key";

    private static final String SIGNATURE_ALGORITHM = "SHA256WithRSA";

    private X509Certificate caCertificate;
    private PrivateKey caPrivateKey;


    public CertificateAuthority() {
        // intentionally left empty
    }

    public X509Certificate getCACertificate() {
        if (this.caCertificate == null) {
            try (InputStream istream = new FileInputStream(CA_CERT_PATH)) {
                CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
                this.caCertificate = (X509Certificate) certFactory.generateCertificate(istream);
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return this.caCertificate;
    }

    public PrivateKey getCAPrivateKey() {
        if (this.caPrivateKey == null) {
            try {
                String key = Files.readString(new File(CA_PRIVATE_KEY_PATH).toPath());

                String pem = key.replaceAll("-----(BEGIN|END) PRIVATE KEY-----", "")
                  .replace(System.lineSeparator(), "");

                byte[] encoded = Base64.getDecoder().decode(pem);

                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                this.caPrivateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(encoded));
            }
            catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
                // TODO: throw a proper exception here
                throw new RuntimeException(e);
            }
        }

        return this.caPrivateKey;
    }

    /**
     * Signs the given array of bytes using the configured CA key.
     *
     * @param bytes
     *  the bytes to sign
     *
     * @return
     *  a SHA256-RSA signature of the given bytes
     */
    public byte[] sign(byte[] bytes) {
        if (bytes == null) {
            throw new IllegalArgumentException("byte array is null");
        }

        try {
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initSign(this.getCAPrivateKey());

            signature.update(bytes, 0, bytes.length);
            return signature.sign();
        }
        catch (Exception e) {
            // TODO: Turn this into a proper exception
            throw new RuntimeException(e);
        }
    }

}
