package com.redhat.graviton.certs;

import io.smallrye.config.ConfigMapping;
import java.nio.file.Path;

@ConfigMapping(prefix = "graviton.certs")
public interface CertsSettings {

    Path ca();
    Path privateKey();
    String signatureAlgorithm();
}
