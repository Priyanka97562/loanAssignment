package com.affirm.loan;

import com.affirm.loan.service.AssignLoanService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        ApplicationContext applicationContext = SpringApplication.run(Application.class, args);
        AssignLoanService assignLoanService = applicationContext.getBean(AssignLoanService.class);
        assignLoanService.assignLoan();
    }
}
