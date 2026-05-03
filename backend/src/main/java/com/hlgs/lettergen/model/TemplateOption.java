package com.hlgs.lettergen.model;

import java.io.Serializable;

public class TemplateOption implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private String templatePath;

    public TemplateOption() {
    }

    public TemplateOption(String name, String templatePath) {
        this.name = name;
        this.templatePath = templatePath;
    }

    public String getName() {
        return name;
    }

    public String getTemplatePath() {
        return templatePath;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTemplatePath(String templatePath) {
        this.templatePath = templatePath;
    }
}

// Made with Bob
