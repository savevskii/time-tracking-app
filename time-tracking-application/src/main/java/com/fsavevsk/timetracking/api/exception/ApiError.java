package com.fsavevsk.timetracking.api.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldError> fieldErrors
) {
    public static ApiError of(int status, String error, String message, String path) {
        return new ApiError(OffsetDateTime.now(), status, error, message, path, null);
    }
    public static ApiError of(int status, String error, String message, String path, List<FieldError> fields) {
        return new ApiError(OffsetDateTime.now(), status, error, message, path, fields);
    }
}
