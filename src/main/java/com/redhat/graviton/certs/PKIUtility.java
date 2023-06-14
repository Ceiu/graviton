package com.redhat.graviton.certs;

import com.redhat.graviton.db.model.KeyPairData;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.io.StringWriter;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;



// TODO:
// This should become an interface, and have different implementations depending on how/where we
// wish to generate keys

@Singleton
public class PKIUtility {
    private static final Logger LOG = Logger.getLogger(PKIUtility.class);

    public static final Provider PROVIDER = new BouncyCastleProvider();

    private String encode(Object obj) {
        if (obj == null) {
            return null;
        }

        try {
            StringWriter strWriter = new StringWriter(1024);
            JcaPEMWriter pemWriter = new JcaPEMWriter(strWriter);

            pemWriter.writeObject(obj);
            pemWriter.flush();
            pemWriter.close();

            return strWriter.toString();
        }
        catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    public String toPemEncodedString(X509Certificate cert) {
        return this.encode(cert);
    }

    public String toPemEncodedString(Key key) {
        return this.encode(key);
    }

    /**
     * Generates a PublicKey instance from the provided key data and algorithm.
     *
     * @param keydata
     *  the X509 encoded public key data from which to generate a PublicKey instance
     *
     * @param algorithm
     *  the algorithm used to generate the key
     *
     * @return
     *  a PublicKey instance
     */
    private PublicKey buildPublicKey(byte[] keydata, String algorithm)
        throws NoSuchAlgorithmException, InvalidKeySpecException {

        KeyFactory factory = KeyFactory.getInstance(algorithm, PROVIDER);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keydata, algorithm);

        return factory.generatePublic(spec);
    }

    /**
     * Generates a PrivateKey instance from the provided key data and algorithm.
     *
     * @param keydata
     *  the PKCS8 private key data from which to generate a PrivateKey instance
     *
     * @param algorithm
     *  the algorithm used to generate the key
     *
     * @return
     *  a PrivateKey instance
     */
    private PrivateKey buildPrivateKey(byte[] keydata, String algorithm)
        throws NoSuchAlgorithmException, InvalidKeySpecException {

        KeyFactory factory = KeyFactory.getInstance(algorithm, PROVIDER);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keydata, algorithm);

        return factory.generatePrivate(spec);
    }

    private byte[] extractKeyData(String pemEncodedKey) {
        pemEncodedKey = pemEncodedKey
            .replaceAll("-----(BEGIN|END) (RSA )?(PUBLIC|PRIVATE) KEY-----", "")
            .replace(System.lineSeparator(), "");

        return Base64.getDecoder().decode(pemEncodedKey);
    }

    public KeyPair convertToKeyPair(KeyPairData kpdata) {
        if (kpdata == null) {
            return null;
        }

        try {
            byte[] publicKeyData = this.extractKeyData(kpdata.getPublicKey());
            PublicKey publicKey = this.buildPublicKey(publicKeyData, "RSA");

            byte[] privateKeyData = this.extractKeyData(kpdata.getPrivateKey());
            PrivateKey privateKey = this.buildPrivateKey(privateKeyData, "RSA");

            return new KeyPair(publicKey, privateKey);
        }
        catch (GeneralSecurityException gse) {
            // TODO: better exception handling here
            throw new RuntimeException(gse);
        }
    }

}
