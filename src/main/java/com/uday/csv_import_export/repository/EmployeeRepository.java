package com.uday.csv_import_export.repository;

import com.uday.csv_import_export.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee,Long> {

    boolean existsByEmail(String email);

    List<Employee> findByEmailIn(
            Collection<String> emails
    );
}
