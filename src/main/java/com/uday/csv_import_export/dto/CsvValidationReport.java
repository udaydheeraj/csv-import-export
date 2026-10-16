package com.uday.csv_import_export.dto;

import java.util.List;

public record CsvValidationReport(
       long totalRows,
        long validRows,
        long invalidRows,
       List<RowErrorDto> errors
) {
}
