package com.hlgs.lettergen.service;

import com.hlgs.lettergen.model.AddressValidationResult;
import com.hlgs.lettergen.model.LetterRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AddressValidationServiceTest {

    private final AddressValidationService service = new AddressValidationService();

    @Test
    void shouldRejectMissingAddressLine1() {
        LetterRequest request = validRequest();
        request.setAddressLine1(" ");

        AddressValidationResult result = service.validate(request);

        assertFalse(result.isValid());
        assertEquals("Address Line 1 is required.", result.getMessage());
    }

    @Test
    void shouldRejectMissingCity() {
        LetterRequest request = validRequest();
        request.setCity(" ");

        AddressValidationResult result = service.validate(request);

        assertFalse(result.isValid());
        assertEquals("City is required.", result.getMessage());
    }

    @Test
    void shouldRejectMissingState() {
        LetterRequest request = validRequest();
        request.setState(null);

        AddressValidationResult result = service.validate(request);

        assertFalse(result.isValid());
        assertEquals("State is required.", result.getMessage());
    }

    @Test
    void shouldRejectMissingZipcode() {
        LetterRequest request = validRequest();
        request.setZipcode("");

        AddressValidationResult result = service.validate(request);

        assertFalse(result.isValid());
        assertEquals("Zipcode is required.", result.getMessage());
    }

    @Test
    void shouldRejectInvalidZipcodeFormat() {
        LetterRequest request = validRequest();
        request.setZipcode("12AB");

        AddressValidationResult result = service.validate(request);

        assertFalse(result.isValid());
        assertEquals("Zipcode should contain 5 to 10 digits.", result.getMessage());
    }

    @Test
    void shouldAcceptValidAddress() {
        LetterRequest request = validRequest();

        AddressValidationResult result = service.validate(request);

        assertTrue(result.isValid());
        assertEquals("Address validated successfully.", result.getMessage());
    }

    private LetterRequest validRequest() {
        LetterRequest request = new LetterRequest();
        request.setMemberName("John Doe");
        request.setReferenceNumber("REF-1");
        request.setAddressLine1("123 Main St");
        request.setCity("Austin");
        request.setState("TX");
        request.setZipcode("78701");
        return request;
    }
}

// Made with Bob
