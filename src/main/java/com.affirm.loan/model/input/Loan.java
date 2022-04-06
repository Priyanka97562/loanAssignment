package com.affirm.loan.model.input;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Loan {
    private double interestRate;
    private long amount;
    private int id;
    private double defaultLikeHood;
    private String state;
}
