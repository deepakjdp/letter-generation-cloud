package com.hlgs.lettergen.model;

import java.io.Serializable;

public class AddressValidationResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean valid;
    private String message;

    public AddressValidationResult() {
    }

    public AddressValidationResult(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

// Made with Bob
