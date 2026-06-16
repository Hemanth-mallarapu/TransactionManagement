package com.example.service;

import com.example.entity.Employee;
import com.example.exception.EmployeeException;
import com.example.repository.EmployeeRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmployeeService self; // Self injection

    // ============================================
    // SCENARIO 1 — Basic @Transactional
    // Transaction commits successfully
    // ============================================
    @Transactional
    public Employee saveEmployee(Employee employee) {
        log.info("Saving employee: {}",
                employee.getName());
        return employeeRepository.save(employee);
        // Commits if no exception
    }

    // ============================================
    // SCENARIO 2 — Rollback on RuntimeException
    // ============================================
    @Transactional
    public void saveAndRollback(Employee employee) {
        employeeRepository.save(employee);
        log.info("Employee saved — about to throw exception");

        // Simulating error
        throw new RuntimeException(
                "Something went wrong!");
        // ✅ Rolls back — employee NOT saved
    }

    // ============================================
    // SCENARIO 3 — No rollback on Checked Exception
    // ============================================
    @Transactional
    public void saveWithCheckedException(
            Employee employee) throws Exception {
        employeeRepository.save(employee);
        log.info("Saved — throwing checked exception");

        throw new Exception("Checked Exception!");
        // ❌ Does NOT rollback by default!
        // Employee IS saved even though exception thrown
    }

    // ============================================
    // SCENARIO 4 — Rollback on Checked Exception
    // ============================================
    @Transactional(rollbackFor = EmployeeException.class)
    public void saveWithRollbackForChecked(
            Employee employee) throws Exception {
        employeeRepository.save(employee);
        log.info("Saved — throwing checked exception");

        throw new Exception("Checked Exception!");
        // ✅ Rolls back because rollbackFor specified
    }

    // ============================================
    // SCENARIO 5 — Self invocation problem
    // @Transactional IGNORED
    // ============================================
    public void createEmployeeWithSelfInvocation(
            Employee employee) {
        log.info("Creating employee...");
        saveEmployeeTransactional(employee);
        // ❌ Calls directly — bypasses proxy!
        // @Transactional IGNORED!
    }

    @Transactional
    public void saveEmployeeTransactional(
            Employee employee) {
        employeeRepository.save(employee);
        throw new RuntimeException(
                "Error after save!");
        // ❌ No rollback — proxy bypassed!
    }

    // ============================================
    // SCENARIO 6 — Self injection FIX
    // @Transactional WORKS
    // ============================================
    public void createEmployeeWithSelfProxy(
            Employee employee) {
        log.info("Creating employee via proxy...");
        self.saveEmployeeTransactional(employee);
        // ✅ Goes through proxy — @Transactional works!
    }

    // ============================================
    // SCENARIO 7 — PROPAGATION REQUIRED (Default)
    // Uses existing transaction
    // ============================================
    @Transactional(propagation = Propagation.REQUIRED)
    public void saveWithRequired(Employee employee) {
        employeeRepository.save(employee);
        log.info("Saved with REQUIRED propagation");
        // Uses existing transaction if available
        // Creates new one if not
    }

    // ============================================
    // SCENARIO 8 — PROPAGATION REQUIRES_NEW
    // Always creates new transaction
    // ============================================
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveWithRequiresNew(Employee employee) {
        employeeRepository.save(employee);
        log.info("Saved with REQUIRES_NEW propagation");
        // Always creates NEW transaction
        // Suspends existing transaction
    }

    // ============================================
    // SCENARIO 9 — Nested transactions
    // ============================================
    @Transactional
    public void saveMultipleEmployees(
            List<Employee> employees) {

        // Save first employee
        employeeRepository.save(employees.get(0));
        log.info("First employee saved");

        // Save second with new transaction
        self.saveWithRequiresNew(employees.get(1));
        log.info("Second employee saved");

        // Throw exception — first rolls back
        // but second is already committed!
        throw new RuntimeException("Outer error!");

        // Result:
        // Employee 1 → ROLLED BACK ✅
        // Employee 2 → COMMITTED ✅ (separate transaction)
    }

    // ============================================
    // SCENARIO 10 — Read only transaction
    // ============================================
    @Transactional(readOnly = true)
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
        // Optimized for read — no dirty checking
        // Throws exception if write attempted
    }

    // ============================================
    // SCENARIO 11 — Transaction timeout
    // ============================================
    @Transactional(timeout = 5)
    public void saveWithTimeout(Employee employee)
            throws InterruptedException {
        employeeRepository.save(employee);

        // Simulating slow operation
        Thread.sleep(6000); // 6 seconds

        // ❌ Throws TransactionTimedOutException
        // Rolls back after 5 seconds
    }

    // ============================================
    // SCENARIO 12 — noRollbackFor
    // ============================================
    @Transactional(noRollbackFor =
            EmployeeException.class)
    public void saveWithNoRollback(
            Employee employee) {
        employeeRepository.save(employee);

        throw new EmployeeException(
                "Custom exception — no rollback!");
        // ✅ Employee IS saved!
        // EmployeeException doesn't trigger rollback
    }
}