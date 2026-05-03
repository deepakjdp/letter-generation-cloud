package com.hlgs.lettergen.service;

import com.hlgs.lettergen.model.TemplateOption;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TemplateRegistryServiceTest {

    @Test
    void shouldReturnConfiguredTemplates() {
        TemplateRegistryService service = new TemplateRegistryService();

        List<TemplateOption> templates = service.getTemplates();

        assertEquals(1, templates.size());
        assertEquals("Welcome Letter", templates.get(0).getName());
        assertEquals("templates/welcome-letter-template.docx", templates.get(0).getTemplatePath());
    }

    @Test
    void shouldFindTemplateByName() {
        TemplateRegistryService service = new TemplateRegistryService();

        TemplateOption option = service.findByName("Welcome Letter");

        assertNotNull(option);
        assertEquals("Welcome Letter", option.getName());
    }

    @Test
    void shouldReturnNullWhenTemplateNameDoesNotExist() {
        TemplateRegistryService service = new TemplateRegistryService();

        TemplateOption option = service.findByName("Unknown");

        assertNull(option);
    }

    @Test
    void shouldReturnUnmodifiableTemplateList() {
        TemplateRegistryService service = new TemplateRegistryService();

        List<TemplateOption> templates = service.getTemplates();

        assertThrows(UnsupportedOperationException.class,
                () -> templates.add(new TemplateOption("Other", "path")));
    }
}

// Made with Bob
