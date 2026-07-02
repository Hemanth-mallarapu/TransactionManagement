package com.example.transaction.controller;

import com.example.transaction.entity.Employee;
import com.example.transaction.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api/employees")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @PostMapping("/save")
    public ResponseEntity<?> save(
            @RequestBody Employee employee) {
        try {
            Employee saved =
                    employeeService.saveEmployee(employee);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // Test Scenario 2 — Rollback
    @PostMapping("/save-rollback")
    public ResponseEntity<?> saveAndRollback(
            @RequestBody Employee employee) {
        try {
            employeeService.saveAndRollback(employee);
            return ResponseEntity.ok("Saved");
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body("Rolled back: " + e.getMessage());
        }
    }

    @PostMapping("/save-checked")
    public ResponseEntity<?> saveWithChecked(@RequestBody Employee employee) {
        try {
            employeeService.saveWithCheckedException(employee);
            return ResponseEntity.ok("Saved");
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body("Exception : " + e.getMessage());
        }

    }

    @PostMapping("/save-checked-rollback")
    public ResponseEntity<?> saveWithCheckedExceptionRollback(@RequestBody Employee employee) {
        try {
            employeeService.saveWithRollbackForChecked(employee);

            return ResponseEntity.ok("Saved");

        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body("Rolled back with Checked Exception : " + e.getMessage());
        }
    }

    @PostMapping("/save-self-invocation")
    public ResponseEntity<?> saveWithOutSelfInvocation(@RequestBody Employee employee) {
        try {
            employeeService.createEmployeeWithSelfInvocation(employee);
            return ResponseEntity.ok("Saved");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Exception raised due to self Invocation Transaction Ignored : " + e.getMessage());
        }
    }

    @PostMapping("/save-self-proxy")
    public ResponseEntity<?> saveWithSelfInvocation(@RequestBody Employee employee) {
        try {
            employeeService.createEmployeeWithSelfProxy(employee);
            return ResponseEntity.ok("Saved");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Exception raised due to self Invocation : " + e.getMessage());
        }
    }
    @PostMapping("/saveMultipleEmployees")
    public ResponseEntity<?> savePropagationRequired(@RequestBody Employee employee) {
        try {
            employeeService.saveWithRequired(employee);
            return ResponseEntity.ok("Saved");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Exception " + e.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

}
