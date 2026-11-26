package com.myhub.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "myhub.storage")
public class StorageProperties {
    /** Absolute or relative root directory for uploaded files. */
    private String path = "./myhub-storage";

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
}
