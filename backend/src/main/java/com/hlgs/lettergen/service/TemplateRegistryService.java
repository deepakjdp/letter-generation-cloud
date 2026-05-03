package com.hlgs.lettergen.service;

import com.hlgs.lettergen.model.TemplateOption;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class TemplateRegistryService {

    private final List<TemplateOption> templates = new ArrayList<>();

    public TemplateRegistryService() {
        templates.add(new TemplateOption("Welcome Letter", "templates/welcome-letter-template.docx"));
    }

    public List<TemplateOption> getTemplates() {
        return Collections.unmodifiableList(templates);
    }

    public TemplateOption findByName(String name) {
        for (TemplateOption option : templates) {
            if (option.getName().equals(name)) {
                return option;
            }
        }
        return null;
    }
}

// Made with Bob
