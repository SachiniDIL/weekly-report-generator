package com.weeklyreport.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

/** Uniform error body. {@code fieldErrors} is populated only for validation failures. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(String message, Map<String, String> fieldErrors) {

    public static ErrorResponse of(String message) {
        return new ErrorResponse(message, null);
    }
}
