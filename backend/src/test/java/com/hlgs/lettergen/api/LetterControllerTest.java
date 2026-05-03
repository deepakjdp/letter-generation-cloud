package com.hlgs.lettergen.api;

import com.hlgs.lettergen.model.AddressValidationResult;
import com.hlgs.lettergen.model.DependentInfo;
import com.hlgs.lettergen.model.LetterRecord;
import com.hlgs.lettergen.model.LetterRequest;
import com.hlgs.lettergen.model.TemplateOption;
import com.hlgs.lettergen.service.AddressValidationService;
import com.hlgs.lettergen.service.LetterGenerationService;
import com.hlgs.lettergen.service.PdfRenderService;
import com.hlgs.lettergen.service.TemplateRegistryService;
import com.hlgs.lettergen.store.LetterMetadataStore;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LetterControllerTest {

    @Test
    void shouldReturnTemplates() {
        TemplateRegistryService templateRegistryService = mock(TemplateRegistryService.class);
        AddressValidationService addressValidationService = mock(AddressValidationService.class);
        PdfRenderService pdfRenderService = mock(PdfRenderService.class);
        LetterGenerationService letterGenerationService = mock(LetterGenerationService.class);
        LetterMetadataStore metadataStore = mock(LetterMetadataStore.class);

        when(templateRegistryService.getTemplates())
                .thenReturn(List.of(new TemplateOption("Welcome Letter", "templates/welcome-letter-template.docx")));

        LetterController controller = new LetterController(
                templateRegistryService, addressValidationService, pdfRenderService, letterGenerationService, metadataStore);

        List<TemplateOption> result = controller.getTemplates();

        assertEquals(1, result.size());
        assertEquals("Welcome Letter", result.get(0).getName());
    }

    @Test
    void shouldRejectAddressValidationWhenNoDependents() {
        LetterController controller = controllerWithMocks();
        LetterRequest request = new LetterRequest();

        AddressValidationResult result = controller.validateAddress(request);

        assertFalse(result.isValid());
        assertEquals("At least one dependent is required before validating the address.", result.getMessage());
    }

    @Test
    void shouldValidateAddressWhenDependentsExist() {
        TemplateRegistryService templateRegistryService = mock(TemplateRegistryService.class);
        AddressValidationService addressValidationService = mock(AddressValidationService.class);
        PdfRenderService pdfRenderService = mock(PdfRenderService.class);
        LetterGenerationService letterGenerationService = mock(LetterGenerationService.class);
        LetterMetadataStore metadataStore = mock(LetterMetadataStore.class);
        when(addressValidationService.validate(any(LetterRequest.class)))
                .thenReturn(new AddressValidationResult(true, "ok"));

        LetterController controller = new LetterController(
                templateRegistryService, addressValidationService, pdfRenderService, letterGenerationService, metadataStore);

        LetterRequest request = requestWithDependent();

        AddressValidationResult result = controller.validateAddress(request);

        assertTrue(result.isValid());
        assertEquals("ok", result.getMessage());
    }

    @Test
    void shouldRejectPreviewWhenNoDependents() {
        LetterController controller = controllerWithMocks();

        ResponseEntity<String> response = controller.previewLetter(new LetterRequest());

        assertEquals(400, response.getStatusCode().value());
        assertEquals("At least one dependent is required before viewing the letter.", response.getBody());
    }

    @Test
    void shouldRejectPreviewWhenAddressInvalid() {
        TemplateRegistryService templateRegistryService = mock(TemplateRegistryService.class);
        AddressValidationService addressValidationService = mock(AddressValidationService.class);
        PdfRenderService pdfRenderService = mock(PdfRenderService.class);
        LetterGenerationService letterGenerationService = mock(LetterGenerationService.class);
        LetterMetadataStore metadataStore = mock(LetterMetadataStore.class);
        when(addressValidationService.validate(any(LetterRequest.class)))
                .thenReturn(new AddressValidationResult(false, "bad"));

        LetterController controller = new LetterController(
                templateRegistryService, addressValidationService, pdfRenderService, letterGenerationService, metadataStore);

        ResponseEntity<String> response = controller.previewLetter(requestWithDependent());

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Validate address before previewing the letter.", response.getBody());
    }

    @Test
    void shouldReturnPreviewHtmlWhenValid() {
        TemplateRegistryService templateRegistryService = mock(TemplateRegistryService.class);
        AddressValidationService addressValidationService = mock(AddressValidationService.class);
        PdfRenderService pdfRenderService = mock(PdfRenderService.class);
        LetterGenerationService letterGenerationService = mock(LetterGenerationService.class);
        LetterMetadataStore metadataStore = mock(LetterMetadataStore.class);
        when(addressValidationService.validate(any(LetterRequest.class)))
                .thenReturn(new AddressValidationResult(true, "ok"));
        when(pdfRenderService.buildPreviewHtml(any(LetterRequest.class))).thenReturn("<html>preview</html>");

        LetterController controller = new LetterController(
                templateRegistryService, addressValidationService, pdfRenderService, letterGenerationService, metadataStore);

        ResponseEntity<String> response = controller.previewLetter(requestWithDependent());

        assertEquals(200, response.getStatusCode().value());
        assertEquals("<html>preview</html>", response.getBody());
    }

    @Test
    void shouldRejectGenerateWhenNoDependents() {
        LetterController controller = controllerWithMocks();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> controller.generateLetter(new LetterRequest()));

        assertEquals("At least one dependent is required before generating the letter.", exception.getMessage());
    }

    @Test
    void shouldRejectGenerateWhenAddressInvalid() {
        TemplateRegistryService templateRegistryService = mock(TemplateRegistryService.class);
        AddressValidationService addressValidationService = mock(AddressValidationService.class);
        PdfRenderService pdfRenderService = mock(PdfRenderService.class);
        LetterGenerationService letterGenerationService = mock(LetterGenerationService.class);
        LetterMetadataStore metadataStore = mock(LetterMetadataStore.class);
        when(addressValidationService.validate(any(LetterRequest.class)))
                .thenReturn(new AddressValidationResult(false, "bad"));

        LetterController controller = new LetterController(
                templateRegistryService, addressValidationService, pdfRenderService, letterGenerationService, metadataStore);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> controller.generateLetter(requestWithDependent()));

        assertEquals("Validate address before generating the letter.", exception.getMessage());
    }

    @Test
    void shouldGenerateLetterWhenValid() throws Exception {
        TemplateRegistryService templateRegistryService = mock(TemplateRegistryService.class);
        AddressValidationService addressValidationService = mock(AddressValidationService.class);
        PdfRenderService pdfRenderService = mock(PdfRenderService.class);
        LetterGenerationService letterGenerationService = mock(LetterGenerationService.class);
        LetterMetadataStore metadataStore = mock(LetterMetadataStore.class);

        LetterRecord record = new LetterRecord();
        record.setFileName("file.pdf");

        when(addressValidationService.validate(any(LetterRequest.class)))
                .thenReturn(new AddressValidationResult(true, "ok"));
        when(letterGenerationService.generate(any(LetterRequest.class))).thenReturn(record);

        LetterController controller = new LetterController(
                templateRegistryService, addressValidationService, pdfRenderService, letterGenerationService, metadataStore);

        LetterRecord result = controller.generateLetter(requestWithDependent());

        assertEquals("file.pdf", result.getFileName());
    }

    @Test
    void shouldReturnAllLettersWhenNoFilter() {
        TemplateRegistryService templateRegistryService = mock(TemplateRegistryService.class);
        AddressValidationService addressValidationService = mock(AddressValidationService.class);
        PdfRenderService pdfRenderService = mock(PdfRenderService.class);
        LetterGenerationService letterGenerationService = mock(LetterGenerationService.class);
        LetterMetadataStore metadataStore = mock(LetterMetadataStore.class);

        when(metadataStore.findAll()).thenReturn(List.of(new LetterRecord()));

        LetterController controller = new LetterController(
                templateRegistryService, addressValidationService, pdfRenderService, letterGenerationService, metadataStore);

        List<LetterRecord> result = controller.getLetters(" ");

        assertEquals(1, result.size());
        verify(metadataStore).findAll();
    }

    @Test
    void shouldSearchLettersWhenFilterProvided() {
        TemplateRegistryService templateRegistryService = mock(TemplateRegistryService.class);
        AddressValidationService addressValidationService = mock(AddressValidationService.class);
        PdfRenderService pdfRenderService = mock(PdfRenderService.class);
        LetterGenerationService letterGenerationService = mock(LetterGenerationService.class);
        LetterMetadataStore metadataStore = mock(LetterMetadataStore.class);

        when(metadataStore.searchByMemberName("john")).thenReturn(List.of(new LetterRecord()));

        LetterController controller = new LetterController(
                templateRegistryService, addressValidationService, pdfRenderService, letterGenerationService, metadataStore);

        List<LetterRecord> result = controller.getLetters("john");

        assertEquals(1, result.size());
        verify(metadataStore).searchByMemberName("john");
    }

    @Test
    void shouldBuildDownloadResponse() {
        LetterController controller = controllerWithMocks();

        ResponseEntity<Resource> response = controller.downloadLetter("backend/pom.xml", "pom.xml");

        assertEquals(200, response.getStatusCode().value());
        assertEquals("application/pdf", response.getHeaders().getContentType().toString());
        assertTrue(response.getHeaders().getFirst("Content-Disposition").contains("pom.xml"));
        assertNotNull(response.getBody());
    }

    private LetterController controllerWithMocks() {
        return new LetterController(
                mock(TemplateRegistryService.class),
                mock(AddressValidationService.class),
                mock(PdfRenderService.class),
                mock(LetterGenerationService.class),
                mock(LetterMetadataStore.class)
        );
    }

    private LetterRequest requestWithDependent() {
        LetterRequest request = new LetterRequest();
        request.setMemberName("John Doe");
        request.setTemplateName("Welcome Letter");
        request.setReferenceNumber("REF-1");
        request.getDependents().add(new DependentInfo());
        return request;
    }
}

// Made with Bob
