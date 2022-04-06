package com.affirm.loan.model.input;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Facility {
    private double amount;
    private double interestRate;
    private int id;
    private int bankId;

}
