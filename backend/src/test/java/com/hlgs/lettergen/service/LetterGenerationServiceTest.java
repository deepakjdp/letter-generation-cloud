package com.hlgs.lettergen.service;

import com.hlgs.lettergen.model.LetterRecord;
import com.hlgs.lettergen.model.LetterRequest;
import com.hlgs.lettergen.model.TemplateOption;
import com.hlgs.lettergen.store.LetterMetadataStore;
import com.hlgs.lettergen.store.StorageConfig;
import com.hlgs.lettergen.util.DateUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LetterGenerationServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldGenerateLetterAndPersistMetadata() throws Exception {
        TemplateRegistryService templateRegistryService = mock(TemplateRegistryService.class);
        PdfRenderService pdfRenderService = mock(PdfRenderService.class);
        LetterMetadataStore metadataStore = mock(LetterMetadataStore.class);
        StorageConfig storageConfig = new StorageConfig(tempDir.toFile(), "letters-index.csv");

        when(templateRegistryService.findByName("Welcome Letter"))
                .thenReturn(new TemplateOption("Welcome Letter", "templates/welcome-letter-template.docx"));
        when(pdfRenderService.renderPdf(any(LetterRequest.class))).thenReturn("pdf-content".getBytes());

        LetterGenerationService service = new LetterGenerationService(
                templateRegistryService, pdfRenderService, metadataStore, storageConfig);

        LetterRequest request = new LetterRequest();
        request.setTemplateName("Welcome Letter");
        request.setMemberName("John Doe");
        request.setReferenceNumber("REF-1");

        try (MockedStatic<DateUtil> dateUtil = mockStatic(DateUtil.class)) {
            dateUtil.when(DateUtil::nowForFile).thenReturn("20260429_120000");
            dateUtil.when(DateUtil::nowForDisplay).thenReturn("2026-04-29 12:00:00");

            LetterRecord record = service.generate(request);

            assertNotNull(record.getId());
            assertEquals("John Doe", record.getMemberName());
            assertEquals("Welcome Letter", record.getTemplateName());
            assertEquals("REF-1", record.getReferenceNumber());
            assertEquals("2026-04-29 12:00:00", record.getGeneratedAt());
            assertEquals("letter_20260429_120000.pdf", record.getFileName());
            assertTrue(Files.exists(tempDir.resolve("letter_20260429_120000.pdf")));
            assertEquals("pdf-content", Files.readString(tempDir.resolve("letter_20260429_120000.pdf")));

            verify(metadataStore).save(any(LetterRecord.class));
        }
    }

    @Test
    void shouldRejectInvalidTemplate() {
        TemplateRegistryService templateRegistryService = mock(TemplateRegistryService.class);
        PdfRenderService pdfRenderService = mock(PdfRenderService.class);
        LetterMetadataStore metadataStore = mock(LetterMetadataStore.class);
        StorageConfig storageConfig = new StorageConfig(tempDir.toFile(), "letters-index.csv");

        when(templateRegistryService.findByName("Invalid")).thenReturn(null);

        LetterGenerationService service = new LetterGenerationService(
                templateRegistryService, pdfRenderService, metadataStore, storageConfig);

        LetterRequest request = new LetterRequest();
        request.setTemplateName("Invalid");
        request.setMemberName("John Doe");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.generate(request));

        assertEquals("Invalid template selected.", exception.getMessage());
        verifyNoInteractions(pdfRenderService, metadataStore);
    }
}

// Made with Bob
