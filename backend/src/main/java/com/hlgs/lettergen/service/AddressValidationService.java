package com.hlgs.lettergen.service;

import com.hlgs.lettergen.model.AddressValidationResult;
import com.hlgs.lettergen.model.LetterRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AddressValidationService {

    private static final Logger log = LoggerFactory.getLogger(AddressValidationService.class);

    public AddressValidationResult validate(LetterRequest request) {
        log.info("event=address_validation_started memberName={} referenceNumber={}",
                safe(request.getMemberName()), safe(request.getReferenceNumber()));

        if (isBlank(request.getAddressLine1())) {
            log.warn("event=address_validation_failed reason=missing_address_line1 memberName={}", safe(request.getMemberName()));
            return new AddressValidationResult(false, "Address Line 1 is required.");
        }
        if (isBlank(request.getCity())) {
            log.warn("event=address_validation_failed reason=missing_city memberName={}", safe(request.getMemberName()));
            return new AddressValidationResult(false, "City is required.");
        }
        if (isBlank(request.getState())) {
            log.warn("event=address_validation_failed reason=missing_state memberName={}", safe(request.getMemberName()));
            return new AddressValidationResult(false, "State is required.");
        }
        if (isBlank(request.getZipcode())) {
            log.warn("event=address_validation_failed reason=missing_zipcode memberName={}", safe(request.getMemberName()));
            return new AddressValidationResult(false, "Zipcode is required.");
        }
        if (!request.getZipcode().matches("\\d{5,10}")) {
            log.warn("event=address_validation_failed reason=invalid_zipcode zipcode={} memberName={}",
                    safe(request.getZipcode()), safe(request.getMemberName()));
            return new AddressValidationResult(false, "Zipcode should contain 5 to 10 digits.");
        }

        log.info("event=address_validation_succeeded memberName={} referenceNumber={}",
                safe(request.getMemberName()), safe(request.getReferenceNumber()));
        return new AddressValidationResult(true, "Address validated successfully.");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}

// Made with Bob
