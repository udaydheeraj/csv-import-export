package com.uday.csv_import_export.controller;

import com.uday.csv_import_export.dto.CsvValidationReport;
import com.uday.csv_import_export.dto.UploadValidationResponse;
import com.uday.csv_import_export.service.EmployeeCsvService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;

@RestController
@RequestMapping("/api/employees")
public class EmployeeCSVController {

    public final EmployeeCsvService employeeCsvService;

    public EmployeeCSVController(EmployeeCsvService employeeCsvService) {
        this.employeeCsvService = employeeCsvService;
    }


    @PostMapping(
            value = "/import",
           consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public CsvValidationReport uploadCSV(
            @RequestParam("file") MultipartFile file
            )
    {
        return employeeCsvService.validateUpload(file);
    }

}
