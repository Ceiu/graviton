package com.redhat.graviton.certs;


import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Provider;



// TODO:
// This should become an interface, and have different implementations depending on how/where we
// wish to generate keys

@ApplicationScoped
public class KeyGenerator {

    private static final String KEY_PAIR_ALGORITHM = "RSA";
    private static final int KEY_SIZE = 4096;

    public KeyPair generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", PKIUtility.PROVIDER);
            generator.initialize(KEY_SIZE);

            return generator.generateKeyPair();
        }
        catch (NoSuchAlgorithmException nsae) {
            // TODO: turn this into a real exception
            throw new RuntimeException(nsae);
        }
    }

    // TODO: might need something to load keys from PEM data here as well.

}












