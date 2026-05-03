package com.hlgs.lettergen.service;

import com.hlgs.lettergen.model.DependentInfo;
import com.hlgs.lettergen.model.LetterRequest;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class PdfRenderService {

    public byte[] renderPdf(LetterRequest request) throws Exception {
        String html = buildHtml(request);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.withHtmlContent(html, null);
        builder.toStream(outputStream);
        builder.run();
        return outputStream.toByteArray();
    }

    public String buildPreviewHtml(LetterRequest request) {
        return buildHtml(request);
    }

    private String buildHtml(LetterRequest request) {
        StringBuilder builder = new StringBuilder();
        builder.append("<html><head><style>")
                .append("body{font-family:Arial,sans-serif;font-size:12px;line-height:1.5;}")
                .append("table{width:100%;border-collapse:collapse;margin-top:16px;}")
                .append("td,th{border:1px solid #555;padding:8px;text-align:left;vertical-align:top;}")
                .append("th{background-color:#f0f4f8;}")
                .append(".address-block{margin-bottom:16px;white-space:pre-line;}")
                .append("</style></head><body>")
                .append("<div class=\"address-block\">")
                .append(escape(request.getAddress()))
                .append("</div>")
                .append("<p><strong>Subject: Welcome to ABC Inc. Healthcare</strong></p>")
                .append("<p>Dear ")
                .append(escape(request.getMemberName()))
                .append(",</p>")
                .append("<p>Welcome to ABC Inc. Healthcare.</p>")
                .append("<p>We are pleased to inform you that new family member(s) have been added to your healthcare plan. Please review the dependent details below for your records.</p>")
                .append("<h3>Dependent Information</h3>")
                .append(buildDependentTable(request.getDependents()))
                .append("<h3>Contractual Agreement</h3>")
                .append("<p>By enrolling dependent family member(s) under your health plan with ABC Inc. Healthcare, you acknowledge and agree to the terms, conditions, limitations, and provisions of coverage as outlined in your policy documents and membership agreement.</p>")
                .append("<p>Coverage for enrolled dependents is subject to eligibility verification, plan benefits, provider network availability, and applicable policy rules. It is your responsibility to review all plan materials carefully and ensure that the information provided during enrollment is accurate and complete.</p>")
                .append("<p>Please retain this letter for your records. For complete details regarding benefits, exclusions, claim procedures, and member responsibilities, please refer to your official plan documents or contact ABC Inc. Healthcare Member Services.</p>")
                .append("<p>Sincerely,</p>")
                .append("<p>ABC Inc. Healthcare<br/>Member Services Team</p>")
                .append("</body></html>");
        return builder.toString();
    }

    private String buildDependentTable(List<DependentInfo> dependents) {
        StringBuilder tableBuilder = new StringBuilder();
        tableBuilder.append("<table>")
                .append("<tr><th>Dependent Name</th><th>Relationship</th><th>Enrollment Date</th><th>PCP Name</th><th>Location</th></tr>");

        if (dependents == null || dependents.isEmpty()) {
            tableBuilder.append("<tr><td colspan=\"5\">No dependents provided.</td></tr>");
        } else {
            for (DependentInfo dependent : dependents) {
                tableBuilder.append("<tr><td>")
                        .append(escape(dependent.getDependentName()))
                        .append("</td><td>")
                        .append(escape(dependent.getRelationship()))
                        .append("</td><td>")
                        .append(escape(dependent.getEnrollmentDate()))
                        .append("</td><td>")
                        .append(escape(dependent.getPcpName()))
                        .append("</td><td>")
                        .append(escape(dependent.getLocation()))
                        .append("</td></tr>");
            }
        }

        tableBuilder.append("</table>");
        return tableBuilder.toString();
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&")
                .replace("<", "<")
                .replace(">", ">");
    }
}

// Made with Bob
