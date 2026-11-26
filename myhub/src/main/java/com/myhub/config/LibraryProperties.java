package com.myhub.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "myhub.library")
public class LibraryProperties {
    private long maxFileBytes = 10 * 1024 * 1024L;
    private long maxStorageBytes = 100 * 1024 * 1024L;

    public long getMaxFileBytes() { return maxFileBytes; }
    public void setMaxFileBytes(long maxFileBytes) { this.maxFileBytes = maxFileBytes; }
    public long getMaxStorageBytes() { return maxStorageBytes; }
    public void setMaxStorageBytes(long maxStorageBytes) { this.maxStorageBytes = maxStorageBytes; }
}
