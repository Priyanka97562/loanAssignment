package com.affirm.loan.model.input;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
    @NoArgsConstructor
public class Covenant {
    private int facilityId;
    private double maxDefaultLikelihood;
    private int bankId;
    private String bannedState;

}
