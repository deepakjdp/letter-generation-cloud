package com.hlgs.lettergen.service;

import com.hlgs.lettergen.model.DependentInfo;
import com.hlgs.lettergen.model.LetterRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PdfRenderServiceTest {

    private final PdfRenderService service = new PdfRenderService();

    @Test
    void shouldBuildPreviewHtmlWithEscapedValuesAndDependentTable() {
        LetterRequest request = new LetterRequest();
        request.setAddress("123 Main St <Apt 1>");
        request.setMemberName("John & Jane");
        request.getDependents().add(createDependent("Kid <One>", "Child", "2024-01-01", "Dr & Smith", "Austin"));

        String html = service.buildPreviewHtml(request);

        assertTrue(html.contains("123 Main St <Apt 1>"));
        assertTrue(html.contains("John & Jane"));
        assertTrue(html.contains("Kid <One>"));
        assertTrue(html.contains("Dr & Smith"));
        assertTrue(html.contains("<table>"));
        assertTrue(html.contains("Dependent Information"));
    }

    @Test
    void shouldBuildPreviewHtmlWithNoDependentsMessage() {
        LetterRequest request = new LetterRequest();
        request.setAddress("123 Main St");
        request.setMemberName("John Doe");

        String html = service.buildPreviewHtml(request);

        assertTrue(html.contains("No dependents provided."));
    }

    @Test
    void shouldRenderPdfBytes() throws Exception {
        LetterRequest request = new LetterRequest();
        request.setAddress("123 Main St");
        request.setMemberName("John Doe");
        request.getDependents().add(createDependent("Kid One", "Child", "2024-01-01", "Dr Smith", "Austin"));

        byte[] pdf = service.renderPdf(request);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }

    private DependentInfo createDependent(String name, String relationship, String enrollmentDate,
                                          String pcpName, String location) {
        DependentInfo dependent = new DependentInfo();
        dependent.setDependentName(name);
        dependent.setRelationship(relationship);
        dependent.setEnrollmentDate(enrollmentDate);
        dependent.setPcpName(pcpName);
        dependent.setLocation(location);
        return dependent;
    }
}

// Made with Bob
