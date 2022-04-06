package com.affirm.loan.controllers;

import com.affirm.loan.service.AssignLoanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class AssignLoanController {

    private AssignLoanService assignLoanService;

    @Autowired
    public AssignLoanController(AssignLoanService assignLoanService) {
        this.assignLoanService = assignLoanService;
    }

    @PostMapping("/assign_loan")
    public ResponseEntity assignLoan() {
        log.info("Loan-Assigment:  Received a request to assign loan");
        assignLoanService.assignLoan();
        log.info("Loan-Assigment:  Successful response");
        return new ResponseEntity<>("A folder named \"output\" containing 2 files assignments.csv and yields.csv" +
                " with results has been generated in the project root folder",HttpStatus.CREATED);
    }

}
