package com.uday.csv_import_export.service;

import com.uday.csv_import_export.dto.CsvValidationReport;
import com.uday.csv_import_export.dto.RowErrorDto;
import com.uday.csv_import_export.dto.UploadValidationResponse;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class EmployeeCsvServiceImpl implements EmployeeCsvService
{

    public static final int MAX_FILE_SIZE = 5 * 1024 * 1024;

    public static final Set<String> REQUIRED_HEADERS =
            Set.of("name", "email", "department", "salary");

    public static final Set<String> departments =
            Set.of("IT", "HR", "FINANCE", "OPERATIONS");

    public static final BigDecimal MAX_SALARY =
            new BigDecimal("9999999999.99");

    @Override
    public CsvValidationReport validateUpload(MultipartFile file)
    {

        validateFileMetadata(file);

        List<RowErrorDto> errors = new ArrayList<>();

        long totalRows = 0;
        long validRows = 0;
        long invalidRows = 0;

        String filename = file.getOriginalFilename();

        try(
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
                CSVParser parser = CSVFormat.DEFAULT.builder()
                        .setHeader()
                        .setSkipHeaderRecord(true)
                        .get()
                        .parse(reader)){

            Map<String,String> headers = NormalizeHeaders(parser.getHeaderMap());

          //  validateHeaders(headers);
            validateRequiredHeaders(headers);

           // long totalRows = 0;
            for(CSVRecord record : parser)
            {
                totalRows++;
                int errorCountBeforeRow = errors.size();

                validateName(
                        record.get(headers.get("name")),
                        record.getRecordNumber(),
                        errors
                );
                validateEmail(
                        record.get(headers.get("email")),
                        record.getRecordNumber(),
                        errors
                );
                validateDepartment(
                        record.get(headers.get("department")),
                        record.getRecordNumber(),
                        errors
                );

                validateSalary(
                        record.get(headers.get("salary")),
                        record.getRecordNumber(),
                        errors
                );

                if (errors.size() == errorCountBeforeRow) {
                    validRows++;
                } else {
                    invalidRows++;
                }


            }

        } catch( IOException ex)
        {
            throw new IllegalArgumentException(
                    "Unable to read or parse the CSV file"
            );
        }
        return new CsvValidationReport(
                totalRows,
                validRows,
                invalidRows,
                List.copyOf(errors)
        );
    }

    private void validateSalary(String value, long rowNumber, List<RowErrorDto> errors) {

        if (value == null || value.trim().isEmpty()) {
            errors.add(new RowErrorDto(
                    rowNumber, "salary",
                    "Salary is required"
            ));
            return;
        }

        try {
            BigDecimal salary =
                    new BigDecimal(value.trim());

            if (salary.compareTo(BigDecimal.ZERO) < 0) {
                errors.add(new RowErrorDto(
                        rowNumber, "salary",
                        "Salary cannot be negative"
                ));
                return;
            }

            if (salary.compareTo(MAX_SALARY) > 0) {
                errors.add(new RowErrorDto(
                        rowNumber, "salary",
                        "Salary exceeds the permitted maximum"
                ));
                return;
            }

            if (salary.stripTrailingZeros().scale() > 2) {
                errors.add(new RowErrorDto(
                        rowNumber, "salary",
                        "Salary cannot have more than two decimal places"
                ));
            }

        } catch (NumberFormatException ex) {
            errors.add(new RowErrorDto(
                    rowNumber, "salary",
                    "Salary must be a valid decimal number"
            ));
        }
    }


    private void validateEmail(String email, long recordNumber, List<RowErrorDto> errors) {

        if(email == null | email.trim().isEmpty())
        {
            errors.add(new RowErrorDto(
                    recordNumber,"email","email is required"
            ));

            return;
        }
        String value = email.trim();

        // A practical syntax check for this learning project.
        String emailPattern =
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

        if (!value.matches(emailPattern)) {
            errors.add(new RowErrorDto(
                    recordNumber, "email",
                    "Invalid email address"
            ));
        }

    }

    private void validateDepartment(
            String value,
            long rowNumber,
            List<RowErrorDto> errors) {

        if (value == null || value.trim().isEmpty()) {
            errors.add(new RowErrorDto(
                    rowNumber, "department",
                    "Department is required"
            ));
            return;
        }

        String department = value.trim()
                .toUpperCase(Locale.ROOT);

        if (!departments.contains(department)) {
            errors.add(new RowErrorDto(
                    rowNumber, "department",
                    "Unsupported department"
            ));
        }
    }


    private void validateName(String name, long recordNumber, List<RowErrorDto> errors) {

        if(name == null | name.trim().isEmpty())
        {
            errors.add(new RowErrorDto(
                    recordNumber,"name","name is required"
            ));
            return;
        }
        if(name.trim().length()>100)
        {
            errors.add(new RowErrorDto(
                    recordNumber,"name", "Name must not exceed 100 chars"
            ));
        }
    }



    private void validateRequiredHeaders(Map<String, String> headers) {

        Set<String> missing = new HashSet<>(REQUIRED_HEADERS);

        missing.removeAll(headers.keySet());

        if(!missing.isEmpty())
        {
            throw new IllegalArgumentException(
                    "Missing required headers: " + missing
            );
        }


    }

    private Map<String, String> NormalizeHeaders(Map<String, Integer> headerMap) {

        Map<String,String> normalized = new HashMap<>();

        for(String originalHeader : headerMap.keySet())
        {
            String key = originalHeader.trim().toLowerCase(Locale.ROOT);

            if(normalized.putIfAbsent(key,originalHeader)
                             != null){
                throw new IllegalArgumentException(
                        "Duplicate CSV header: " + originalHeader
                );
            }

        }

        return normalized;

    }

    private void validateHeaders(Map<String, Integer> headerMap)
    {
        Set<String> normalizedHeaders = new HashSet<>();

        for(String header : headerMap.keySet())
        {
            String normalized = header.trim().toLowerCase(Locale.ROOT);

            if(!normalizedHeaders.add(normalized)){
                throw new IllegalArgumentException(
                        "Duplicate CSV header: " + header
                );
            }
        }

        if(!normalizedHeaders.containsAll(REQUIRED_HEADERS))
        {
            Set<String> missingHeaders = new HashSet<>(REQUIRED_HEADERS);

            missingHeaders.removeAll(normalizedHeaders);

            throw new IllegalArgumentException("Missing required Headers" + missingHeaders);

        }

    }

    private void validateFileMetadata(MultipartFile file)
    {


        if(file == null || file.isEmpty())
        {
            throw new IllegalArgumentException("please upload non-empty CSV file");
        }

        if(file.getSize() > MAX_FILE_SIZE)
        {
            throw new IllegalArgumentException("file must not Exceed 5MB");
        }

        String filename = file.getOriginalFilename();

        if(filename == null || !filename.toLowerCase(Locale.ROOT).endsWith(".csv"))
        {
            throw new IllegalArgumentException("Only CSV Files are Supported");
        }
    }

}



