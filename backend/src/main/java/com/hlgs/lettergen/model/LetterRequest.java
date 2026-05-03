package com.hlgs.lettergen.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LetterRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String templateName;
    private String memberName;
    private String referenceNumber;
    private String subject;
    private String address;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String zipcode;
    private String effectiveDate;
    private String remarks;
    private final Map<String, String> customFields = new LinkedHashMap<>();
    private final List<DependentInfo> dependents = new ArrayList<>();

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
        rebuildAddress();
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
        rebuildAddress();
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
        rebuildAddress();
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
        rebuildAddress();
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
        rebuildAddress();
    }

    public String getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(String effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Map<String, String> getCustomFields() {
        return customFields;
    }

    public List<DependentInfo> getDependents() {
        return dependents;
    }

    private void rebuildAddress() {
        StringBuilder builder = new StringBuilder();
        appendLine(builder, addressLine1);
        appendLine(builder, addressLine2);

        StringBuilder cityStateZip = new StringBuilder();
        appendSegment(cityStateZip, city, "");
        appendSegment(cityStateZip, state, cityStateZip.length() == 0 ? "" : ", ");
        appendSegment(cityStateZip, zipcode, cityStateZip.length() == 0 ? "" : " ");

        if (cityStateZip.length() > 0) {
            appendLine(builder, cityStateZip.toString());
        }

        this.address = builder.toString();
    }

    private void appendLine(StringBuilder builder, String value) {
        if (value != null && !value.trim().isEmpty()) {
            if (builder.length() > 0) {
                builder.append("\n");
            }
            builder.append(value.trim());
        }
    }

    private void appendSegment(StringBuilder builder, String value, String prefix) {
        if (value != null && !value.trim().isEmpty()) {
            builder.append(prefix).append(value.trim());
        }
    }
}

// Made with Bob
