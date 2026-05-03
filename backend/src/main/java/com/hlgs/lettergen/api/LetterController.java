package com.hlgs.lettergen.api;

import com.hlgs.lettergen.model.AddressValidationResult;
import com.hlgs.lettergen.model.LetterRecord;
import com.hlgs.lettergen.model.LetterRequest;
import com.hlgs.lettergen.model.TemplateOption;
import com.hlgs.lettergen.service.AddressValidationService;
import com.hlgs.lettergen.service.LetterGenerationService;
import com.hlgs.lettergen.service.PdfRenderService;
import com.hlgs.lettergen.service.TemplateRegistryService;
import com.hlgs.lettergen.store.LetterMetadataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")
public class LetterController {

    private static final Logger log = LoggerFactory.getLogger(LetterController.class);

    private final TemplateRegistryService templateRegistryService;
    private final AddressValidationService addressValidationService;
    private final PdfRenderService pdfRenderService;
    private final LetterGenerationService letterGenerationService;
    private final LetterMetadataStore letterMetadataStore;

    public LetterController(TemplateRegistryService templateRegistryService,
                            AddressValidationService addressValidationService,
                            PdfRenderService pdfRenderService,
                            LetterGenerationService letterGenerationService,
                            LetterMetadataStore letterMetadataStore) {
        this.templateRegistryService = templateRegistryService;
        this.addressValidationService = addressValidationService;
        this.pdfRenderService = pdfRenderService;
        this.letterGenerationService = letterGenerationService;
        this.letterMetadataStore = letterMetadataStore;
    }

    @GetMapping("/templates")
    public List<TemplateOption> getTemplates() {
        log.info("event=get_templates_requested");
        return templateRegistryService.getTemplates();
    }

    @PostMapping("/address/validate")
    public AddressValidationResult validateAddress(@RequestBody LetterRequest request) {
        log.info("event=validate_address_requested memberName={} referenceNumber={}",
                safe(request.getMemberName()), safe(request.getReferenceNumber()));
        if (!hasAtLeastOneDependent(request)) {
            log.warn("event=validate_address_rejected reason=no_dependents memberName={}", safe(request.getMemberName()));
            return new AddressValidationResult(false, "At least one dependent is required before validating the address.");
        }
        return addressValidationService.validate(request);
    }

    @PostMapping("/letters/preview")
    public ResponseEntity<String> previewLetter(@RequestBody LetterRequest request) {
        log.info("event=preview_letter_requested memberName={} templateName={} referenceNumber={}",
                safe(request.getMemberName()), safe(request.getTemplateName()), safe(request.getReferenceNumber()));
        if (!hasAtLeastOneDependent(request)) {
            log.warn("event=preview_letter_rejected reason=no_dependents memberName={}", safe(request.getMemberName()));
            return ResponseEntity.badRequest().body("At least one dependent is required before viewing the letter.");
        }

        AddressValidationResult validationResult = addressValidationService.validate(request);
        if (!validationResult.isValid()) {
            log.warn("event=preview_letter_rejected reason=address_not_valid memberName={} message={}",
                    safe(request.getMemberName()), safe(validationResult.getMessage()));
            return ResponseEntity.badRequest().body("Validate address before previewing the letter.");
        }

        return ResponseEntity.ok(pdfRenderService.buildPreviewHtml(request));
    }

    @PostMapping("/letters")
    public LetterRecord generateLetter(@RequestBody LetterRequest request) throws Exception {
        log.info("event=generate_letter_requested memberName={} templateName={} referenceNumber={}",
                safe(request.getMemberName()), safe(request.getTemplateName()), safe(request.getReferenceNumber()));
        if (!hasAtLeastOneDependent(request)) {
            log.warn("event=generate_letter_rejected reason=no_dependents memberName={}", safe(request.getMemberName()));
            throw new IllegalArgumentException("At least one dependent is required before generating the letter.");
        }

        AddressValidationResult validationResult = addressValidationService.validate(request);
        if (!validationResult.isValid()) {
            log.warn("event=generate_letter_rejected reason=address_not_valid memberName={} message={}",
                    safe(request.getMemberName()), safe(validationResult.getMessage()));
            throw new IllegalArgumentException("Validate address before generating the letter.");
        }

        return letterGenerationService.generate(request);
    }

    @GetMapping("/letters")
    public List<LetterRecord> getLetters(@RequestParam(name = "memberName", required = false) String memberName) {
        log.info("event=get_letters_requested memberNameFilter={}", safe(memberName));
        if (memberName == null || memberName.trim().isEmpty()) {
            return letterMetadataStore.findAll();
        }
        return letterMetadataStore.searchByMemberName(memberName);
    }

    @GetMapping("/letters/download")
    public ResponseEntity<Resource> downloadLetter(@RequestParam("filePath") String filePath,
                                                   @RequestParam("fileName") String fileName) {
        log.info("event=download_letter_requested fileName={} filePath={}", safe(fileName), safe(filePath));
        File file = new File(filePath);
        Resource resource = new FileSystemResource(file);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\"" + fileName + "\"")
                .contentLength(file.length())
                .body(resource);
    }

    private boolean hasAtLeastOneDependent(LetterRequest request) {
        return request.getDependents() != null && !request.getDependents().isEmpty();
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}

// Made with Bob
