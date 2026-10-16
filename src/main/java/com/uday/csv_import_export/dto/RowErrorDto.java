package com.uday.csv_import_export.dto;

public record RowErrorDto(
        Long rowNumber,
        String field,
        String message
) {
}
