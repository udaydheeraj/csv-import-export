package com.uday.csv_import_export.service;

import com.uday.csv_import_export.dto.CsvValidationReport;
import com.uday.csv_import_export.dto.UploadValidationResponse;
import org.springframework.web.multipart.MultipartFile;

public interface EmployeeCsvService {

    CsvValidationReport validateUpload(MultipartFile file);
}
