package com.hlgs.lettergen.store;

import com.hlgs.lettergen.model.LetterRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LetterMetadataStoreTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldInitializeEmptyStoreAndCreateFiles() throws Exception {
        StorageConfig config = new StorageConfig(tempDir.toFile(), "letters-index.csv");
        LetterMetadataStore store = new LetterMetadataStore(config);

        store.initialize();

        assertTrue(Files.exists(tempDir));
        assertTrue(Files.exists(tempDir.resolve("letters-index.csv")));
        assertTrue(store.findAll().isEmpty());
    }

    @Test
    void shouldLoadExistingRecordsAndSkipInvalidRows() throws Exception {
        Files.writeString(tempDir.resolve("letters-index.csv"),
                "1|John Doe|Welcome Letter|REF-1|2026-01-01|letter.pdf|C:/tmp/letter.pdf\ninvalid");

        StorageConfig config = new StorageConfig(tempDir.toFile(), "letters-index.csv");
        LetterMetadataStore store = new LetterMetadataStore(config);

        store.initialize();

        List<LetterRecord> records = store.findAll();
        assertEquals(1, records.size());
        assertEquals("John Doe", records.get(0).getMemberName());
    }

    @Test
    void shouldSaveRecordAndPersistSanitizedValues() throws Exception {
        StorageConfig config = new StorageConfig(tempDir.toFile(), "letters-index.csv");
        LetterMetadataStore store = new LetterMetadataStore(config);
        store.initialize();

        LetterRecord record = new LetterRecord();
        record.setId("1");
        record.setMemberName("John|Doe");
        record.setTemplateName("Welcome|Letter");
        record.setReferenceNumber("REF|1");
        record.setGeneratedAt("2026-01-01");
        record.setFileName("letter|1.pdf");
        record.setFilePath("C:/tmp|/letter.pdf");

        store.save(record);

        List<LetterRecord> records = store.findAll();
        assertEquals(1, records.size());
        assertEquals("John|Doe", records.get(0).getMemberName());

        String persisted = Files.readString(tempDir.resolve("letters-index.csv"));
        assertTrue(persisted.contains("John Doe"));
        assertTrue(persisted.contains("Welcome Letter"));
        assertTrue(persisted.contains("REF 1"));
    }

    @Test
    void shouldReturnNewestRecordFirst() throws Exception {
        StorageConfig config = new StorageConfig(tempDir.toFile(), "letters-index.csv");
        LetterMetadataStore store = new LetterMetadataStore(config);
        store.initialize();

        LetterRecord first = new LetterRecord();
        first.setId("1");
        first.setMemberName("Alice");
        first.setTemplateName("Welcome Letter");

        LetterRecord second = new LetterRecord();
        second.setId("2");
        second.setMemberName("Bob");
        second.setTemplateName("Welcome Letter");

        store.save(first);
        store.save(second);

        List<LetterRecord> records = store.findAll();
        assertEquals("2", records.get(0).getId());
        assertEquals("1", records.get(1).getId());
    }

    @Test
    void shouldSearchByMemberNameIgnoringCase() throws Exception {
        StorageConfig config = new StorageConfig(tempDir.toFile(), "letters-index.csv");
        LetterMetadataStore store = new LetterMetadataStore(config);
        store.initialize();

        LetterRecord first = new LetterRecord();
        first.setId("1");
        first.setMemberName("Alice Johnson");

        LetterRecord second = new LetterRecord();
        second.setId("2");
        second.setMemberName("Bob Smith");

        store.save(first);
        store.save(second);

        List<LetterRecord> result = store.searchByMemberName("alice");

        assertEquals(1, result.size());
        assertEquals("Alice Johnson", result.get(0).getMemberName());
    }

    @Test
    void shouldReturnUnmodifiableCopyFromFindAll() throws Exception {
        StorageConfig config = new StorageConfig(tempDir.toFile(), "letters-index.csv");
        LetterMetadataStore store = new LetterMetadataStore(config);
        store.initialize();

        List<LetterRecord> records = store.findAll();

        assertThrows(UnsupportedOperationException.class, () -> records.add(new LetterRecord()));
    }
}

// Made with Bob
