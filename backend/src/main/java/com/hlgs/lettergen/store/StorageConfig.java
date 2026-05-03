package com.hlgs.lettergen.store;

import java.io.File;
import java.io.Serializable;

public class StorageConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    private final File baseDirectory;
    private final File metadataFile;

    public StorageConfig(File baseDirectory, String metadataFileName) {
        this.baseDirectory = baseDirectory;
        this.metadataFile = new File(baseDirectory, metadataFileName);
    }

    public File getBaseDirectory() {
        return baseDirectory;
    }

    public File getMetadataFile() {
        return metadataFile;
    }
}

// Made with Bob
