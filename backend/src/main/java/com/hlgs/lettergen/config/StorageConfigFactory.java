package com.hlgs.lettergen.config;

import com.hlgs.lettergen.store.StorageConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class StorageConfigFactory {

    @Bean
    public StorageConfig storageConfig(StorageProperties properties) {
        return new StorageConfig(new File(properties.getBaseDirectory()), properties.getMetadataFileName());
    }
}

// Made with Bob
