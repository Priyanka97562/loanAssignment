package com.affirm.loan.model.output;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Yield {
    private int facilityId;
    private int expectedYield;

}
