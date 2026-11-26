package com.myhub.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({StorageProperties.class, LibraryProperties.class})
public class MyHubPropertiesConfig {
}
