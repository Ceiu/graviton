package com.redhat.graviton.impl.datasource.fs;

import io.smallrye.config.ConfigMapping;
import java.nio.file.Path;

@ConfigMapping(prefix = "graviton.datasource.fs")
public interface FileSystemDataSourceSettings {

    Path subscriptions();
    Path products();
    Path raw();
}
