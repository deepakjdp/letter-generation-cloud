package com.hlgs.lettergen.model;

import com.hlgs.lettergen.LetterGenApplication;
import com.hlgs.lettergen.config.ApplicationConfig;
import com.hlgs.lettergen.config.StorageConfigFactory;
import com.hlgs.lettergen.config.StorageProperties;
import com.hlgs.lettergen.store.StorageConfig;
import com.hlgs.lettergen.util.DateUtil;
import com.hlgs.lettergen.util.FileUtil;
import org.junit.jupiter.api.Test;

import java.io.Closeable;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ModelConfigUtilTest {

    @Test
    void shouldRunApplicationMain() {
        assertDoesNotThrow(() -> LetterGenApplication.main(new String[] {"--spring.main.web-application-type=none"}));
    }

    @Test
    void shouldHandleAddressValidationResultBean() {
        AddressValidationResult result = new AddressValidationResult();
        result.setValid(true);
        result.setMessage("ok");

        assertTrue(result.isValid());
        assertEquals("ok", result.getMessage());

        AddressValidationResult constructed = new AddressValidationResult(false, "bad");
        assertFalse(constructed.isValid());
        assertEquals("bad", constructed.getMessage());
    }

    @Test
    void shouldHandleDependentInfoBean() {
        DependentInfo info = new DependentInfo();
        info.setDependentName("Kid");
        info.setRelationship("Child");
        info.setEnrollmentDate("2026-01-01");
        info.setPcpName("Dr Smith");
        info.setLocation("Austin");

        assertEquals("Kid", info.getDependentName());
        assertEquals("Child", info.getRelationship());
        assertEquals("2026-01-01", info.getEnrollmentDate());
        assertEquals("Dr Smith", info.getPcpName());
        assertEquals("Austin", info.getLocation());
    }

    @Test
    void shouldHandleLetterRecordBean() {
        LetterRecord record = new LetterRecord();
        record.setId("1");
        record.setMemberName("John");
        record.setTemplateName("Welcome");
        record.setReferenceNumber("REF-1");
        record.setGeneratedAt("2026-01-01");
        record.setFileName("letter.pdf");
        record.setFilePath("/tmp/letter.pdf");

        assertEquals("1", record.getId());
        assertEquals("John", record.getMemberName());
        assertEquals("Welcome", record.getTemplateName());
        assertEquals("REF-1", record.getReferenceNumber());
        assertEquals("2026-01-01", record.getGeneratedAt());
        assertEquals("letter.pdf", record.getFileName());
        assertEquals("/tmp/letter.pdf", record.getFilePath());
    }

    @Test
    void shouldHandleTemplateOptionBean() {
        TemplateOption option = new TemplateOption();
        option.setName("Welcome");
        option.setTemplatePath("templates/welcome.docx");

        assertEquals("Welcome", option.getName());
        assertEquals("templates/welcome.docx", option.getTemplatePath());

        TemplateOption constructed = new TemplateOption("Name", "Path");
        assertEquals("Name", constructed.getName());
        assertEquals("Path", constructed.getTemplatePath());
    }

    @Test
    void shouldRebuildLetterRequestAddressAndExposeCollections() {
        LetterRequest request = new LetterRequest();
        request.setTemplateName("Welcome");
        request.setMemberName("John");
        request.setReferenceNumber("REF-1");
        request.setSubject("Subject");
        request.setAddressLine1("123 Main");
        request.setAddressLine2("Apt 4");
        request.setCity("Austin");
        request.setState("TX");
        request.setZipcode("78701");
        request.setEffectiveDate("2026-01-01");
        request.setRemarks("Remark");
        request.getCustomFields().put("k", "v");
        request.getDependents().add(new DependentInfo());

        assertEquals("Welcome", request.getTemplateName());
        assertEquals("John", request.getMemberName());
        assertEquals("REF-1", request.getReferenceNumber());
        assertEquals("Subject", request.getSubject());
        assertTrue(request.getAddress().contains("123 Main"));
        assertTrue(request.getAddress().contains("Apt 4"));
        assertTrue(request.getAddress().contains("Austin, TX 78701"));
        assertEquals("2026-01-01", request.getEffectiveDate());
        assertEquals("Remark", request.getRemarks());
        assertEquals("v", request.getCustomFields().get("k"));
        assertEquals(1, request.getDependents().size());

        request.setAddress("Manual Address");
        assertEquals("Manual Address", request.getAddress());
    }

    @Test
    void shouldHandleStoragePropertiesAndFactory() {
        StorageProperties properties = new StorageProperties();
        properties.setBaseDirectory("data");
        properties.setMetadataFileName("letters.csv");

        assertEquals("data", properties.getBaseDirectory());
        assertEquals("letters.csv", properties.getMetadataFileName());

        StorageConfigFactory factory = new StorageConfigFactory();
        StorageConfig config = factory.storageConfig(properties);

        assertEquals("data", config.getBaseDirectory().getPath());
        assertTrue(config.getMetadataFile().getPath().endsWith("data" + java.io.File.separator + "letters.csv"));
    }

    @Test
    void shouldHandleStorageConfigAndApplicationConfig() {
        StorageConfig config = new StorageConfig(new java.io.File("base"), "file.csv");

        assertEquals("base", config.getBaseDirectory().getPath());
        assertTrue(config.getMetadataFile().getPath().endsWith("base" + java.io.File.separator + "file.csv"));

        assertNotNull(new ApplicationConfig());
    }

    @Test
    void shouldHandleFileUtilMethods() {
        assertEquals("", FileUtil.safeValue(null));
        assertEquals("abc", FileUtil.safeValue("  abc  "));

        Closeable closeable = new Closeable() {
            @Override
            public void close() throws IOException {
                throw new IOException("ignored");
            }
        };

        assertDoesNotThrow(() -> FileUtil.closeQuietly(null));
        assertDoesNotThrow(() -> FileUtil.closeQuietly(closeable));
    }

    @Test
    void shouldReturnFormattedDates() {
        String fileFormat = DateUtil.nowForFile();
        String displayFormat = DateUtil.nowForDisplay();

        assertTrue(fileFormat.matches("\\d{8}_\\d{6}"));
        assertTrue(displayFormat.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
    }
}

// Made with Bob
