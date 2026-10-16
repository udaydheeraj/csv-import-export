package com.uday.csv_import_export.dto;

public record UploadValidationResponse(
        String message,
        String filename,
        long totalRows
) {
}
