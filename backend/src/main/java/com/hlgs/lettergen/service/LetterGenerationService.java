package com.hlgs.lettergen.service;

import com.hlgs.lettergen.model.LetterRecord;
import com.hlgs.lettergen.model.LetterRequest;
import com.hlgs.lettergen.model.TemplateOption;
import com.hlgs.lettergen.store.LetterMetadataStore;
import com.hlgs.lettergen.store.StorageConfig;
import com.hlgs.lettergen.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

@Service
public class LetterGenerationService {

    private static final Logger log = LoggerFactory.getLogger(LetterGenerationService.class);

    private final TemplateRegistryService templateRegistryService;
    private final PdfRenderService pdfRenderService;
    private final LetterMetadataStore metadataStore;
    private final StorageConfig storageConfig;

    public LetterGenerationService(TemplateRegistryService templateRegistryService,
                                   PdfRenderService pdfRenderService,
                                   LetterMetadataStore metadataStore,
                                   StorageConfig storageConfig) {
        this.templateRegistryService = templateRegistryService;
        this.pdfRenderService = pdfRenderService;
        this.metadataStore = metadataStore;
        this.storageConfig = storageConfig;
    }

    public LetterRecord generate(LetterRequest request) throws Exception {
        log.info("event=letter_generation_started memberName={} templateName={} referenceNumber={}",
                safe(request.getMemberName()), safe(request.getTemplateName()), safe(request.getReferenceNumber()));

        TemplateOption option = templateRegistryService.findByName(request.getTemplateName());
        if (option == null) {
            log.warn("event=letter_generation_failed reason=invalid_template templateName={} memberName={}",
                    safe(request.getTemplateName()), safe(request.getMemberName()));
            throw new IllegalArgumentException("Invalid template selected.");
        }

        String id = UUID.randomUUID().toString();
        String fileName = "letter_" + DateUtil.nowForFile() + ".pdf";
        File outputFile = new File(storageConfig.getBaseDirectory(), fileName);
        byte[] pdfBytes = pdfRenderService.renderPdf(request);

        FileOutputStream outputStream = new FileOutputStream(outputFile);
        try {
            outputStream.write(pdfBytes);
        } finally {
            outputStream.close();
        }

        LetterRecord record = new LetterRecord();
        record.setId(id);
        record.setMemberName(request.getMemberName());
        record.setTemplateName(option.getName());
        record.setReferenceNumber(request.getReferenceNumber());
        record.setGeneratedAt(DateUtil.nowForDisplay());
        record.setFileName(fileName);
        record.setFilePath(outputFile.getAbsolutePath());

        metadataStore.save(record);
        log.info("event=letter_generation_succeeded letterId={} fileName={} filePath={} memberName={}",
                record.getId(), record.getFileName(), record.getFilePath(), safe(record.getMemberName()));
        return record;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}

// Made with Bob
