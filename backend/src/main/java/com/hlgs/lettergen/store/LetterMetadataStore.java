package com.hlgs.lettergen.store;

import com.hlgs.lettergen.model.LetterRecord;
import com.hlgs.lettergen.util.FileUtil;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class LetterMetadataStore {

    private static final Logger log = LoggerFactory.getLogger(LetterMetadataStore.class);

    private final StorageConfig storageConfig;
    private final List<LetterRecord> records = new ArrayList<>();

    public LetterMetadataStore(StorageConfig storageConfig) {
        this.storageConfig = storageConfig;
    }

    @PostConstruct
    public synchronized void initialize() throws IOException {
        File baseDir = storageConfig.getBaseDirectory();
        if (!baseDir.exists()) {
            boolean created = baseDir.mkdirs();
            log.info("event=metadata_store_base_dir_created path={} created={}", baseDir.getAbsolutePath(), created);
        }
        File metadataFile = storageConfig.getMetadataFile();
        if (!metadataFile.exists()) {
            boolean created = metadataFile.createNewFile();
            log.info("event=metadata_store_file_created path={} created={}", metadataFile.getAbsolutePath(), created);
        }
        loadRecords(metadataFile);
        log.info("event=metadata_store_initialized path={} recordCount={}", metadataFile.getAbsolutePath(), records.size());
    }

    public synchronized void save(LetterRecord record) throws IOException {
        records.add(0, record);
        persist();
        log.info("event=metadata_store_record_saved letterId={} memberName={} totalRecords={}",
                safe(record.getId()), safe(record.getMemberName()), records.size());
    }

    public synchronized List<LetterRecord> findAll() {
        log.info("event=metadata_store_find_all totalRecords={}", records.size());
        return Collections.unmodifiableList(new ArrayList<>(records));
    }

    public synchronized List<LetterRecord> searchByMemberName(String memberName) {
        List<LetterRecord> result = new ArrayList<>();
        String criteria = FileUtil.safeValue(memberName).toLowerCase();
        for (LetterRecord record : records) {
            if (record.getMemberName() != null && record.getMemberName().toLowerCase().contains(criteria)) {
                result.add(record);
            }
        }
        log.info("event=metadata_store_search criteria={} matchCount={}", criteria, result.size());
        return result;
    }

    private void loadRecords(File metadataFile) throws IOException {
        records.clear();
        BufferedReader reader = new BufferedReader(new FileReader(metadataFile));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|", -1);
                if (parts.length < 7) {
                    log.warn("event=metadata_store_record_skipped reason=invalid_part_count line={}", line);
                    continue;
                }
                LetterRecord record = new LetterRecord();
                record.setId(parts[0]);
                record.setMemberName(parts[1]);
                record.setTemplateName(parts[2]);
                record.setReferenceNumber(parts[3]);
                record.setGeneratedAt(parts[4]);
                record.setFileName(parts[5]);
                record.setFilePath(parts[6]);
                records.add(record);
            }
        } finally {
            FileUtil.closeQuietly(reader);
        }
    }

    private void persist() throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(storageConfig.getMetadataFile(), false));
        try {
            for (LetterRecord record : records) {
                writer.write(safe(record.getId()) + "|"
                        + safe(record.getMemberName()) + "|"
                        + safe(record.getTemplateName()) + "|"
                        + safe(record.getReferenceNumber()) + "|"
                        + safe(record.getGeneratedAt()) + "|"
                        + safe(record.getFileName()) + "|"
                        + safe(record.getFilePath()));
                writer.newLine();
            }
        } finally {
            FileUtil.closeQuietly(writer);
        }
        log.info("event=metadata_store_persisted path={} recordCount={}",
                storageConfig.getMetadataFile().getAbsolutePath(), records.size());
    }

    private String safe(String value) {
        return value == null ? "" : value.replace("|", " ");
    }
}

// Made with Bob
