package com.affirm.loan.service;

import com.affirm.loan.model.input.Bank;
import com.affirm.loan.model.input.Covenant;
import com.affirm.loan.model.input.Facility;
import com.affirm.loan.model.input.Loan;
import com.affirm.loan.util.CSVFormatter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.affirm.loan.util.Constants.*;

@Service
@Slf4j
public class AssignLoanService {

    private CSVFormatter csvFormatter;
    List<Bank> bankList = new ArrayList<>();
    List<Facility> facilityList = new ArrayList<>();
    Map<Integer, Loan> loanMap = new HashMap<>();
    Map<Integer, List<Covenant>> facilityCovenantMap = new HashMap<>();


    @Autowired
    public AssignLoanService(CSVFormatter csvFormatter) {
        this.csvFormatter = csvFormatter;
    }

    public void assignLoan() {
        initData(); // initialize data from input csv files

        // sort facilities in ascending order of interest rates that affirm has to pay to bank so profit can be maximised

        Collections.sort(facilityList, (a,b)-> Double.compare(a.getInterestRate(), b.getInterestRate()));
        HashMap<Integer, Integer> loanFacilityMap = new HashMap<>();

        for (Loan loan: loanMap.values()) {

            for (Facility facility: facilityList) {
                double loanAmountLeft = facility.getAmount() - loan.getAmount();

                if ( loanAmountLeft > 0 && facilityCanFund(facility.getId(), loan) ) {
                    facility.setAmount(loanAmountLeft); // update the loan amount left with facility after approving new one
                    loanFacilityMap.put(loan.getId(), facility.getId());
                    break; // break out of internal loop to check more facilities if we find one match
                }
            }
        }

        generateTotalYield(loanFacilityMap);
        csvFormatter.generateCSVFile(OUTPUT_ASSIGNMENT_FILE_PATH, loanFacilityMap, "loan_id", "facility_id");
    }

    public boolean facilityCanFund(int facilityId, Loan loan) {
        // facilityCovenantMap holds all the covenants of each facility
        List<Covenant> covenantsOfAFacility = facilityCovenantMap.get(facilityId);

        for(Covenant c: covenantsOfAFacility) {
            if (c.getMaxDefaultLikelihood() < loan.getDefaultLikeHood() || c.getBannedState().equalsIgnoreCase(loan.getState()) ) {
                return false;
            }
        }
        return true;
    }

    public void initData() {
        // Bank List
        List<List<String>> bankData = csvFormatter.getRecords(BANK_FILE_PATH);

        // Adding all banks from csv to list
        for (int i = 1; i < bankData.size(); i++) {
            bankList.add(new Bank(Integer.parseInt(bankData.get(i).get(0)),bankData.get(i).get(1)));
        }

        // Facilities List
        List<List<String>> facilitiesData = csvFormatter.getRecords(FACILITY_FILE_PATH);

        // Adding all facilities from csv to facilities list
        for (int i = 1; i < facilitiesData.size(); i++) {
            facilityList.add(new Facility(Double.parseDouble(facilitiesData.get(i).get(0)), Double.parseDouble(facilitiesData.get(i).get(1)),
                    Integer.parseInt(facilitiesData.get(i).get(2)), Integer.parseInt(facilitiesData.get(i).get(3)) ));
        }

        // Loan List
        List<List<String>> loanData = csvFormatter.getRecords(LOAN_FILE_PATH);

        // Adding all loans from csv to loan list
        for (int i = 1; i < loanData.size(); i++) {
            int loanId = Integer.parseInt(loanData.get(i).get(2));
            loanMap.put(loanId, new Loan( Double.parseDouble(loanData.get(i).get(0)), Long.parseLong(loanData.get(i).get(1)),
                    Integer.parseInt(loanData.get(i).get(2)), Double.parseDouble(loanData.get(i).get(3)), loanData.get(i).get(4)));
        }


        // Covenant List
        // From the covenants files, assuming each facility can have multiple covenants
        List<List<String>> covenantData = csvFormatter.getRecords(COVENANT_FILE_PATH);

        // Adding all covenant from csv to covenant HashMap value list as there can multiple covenants for a specific facility
        for (int i = 1; i < covenantData.size(); i++) {
            // if facility_id is empty, there can be multiple facilities served by this particular bank, so we have to add all the facilities to
            // map and override any existing ones
            boolean allFacilities = covenantData.get(i).get(0).isEmpty() ? true : false;
            double maxDefaultLikelihood = covenantData.get(i).get(1).isEmpty() ? Integer.MAX_VALUE : Double.parseDouble(covenantData.get(i).get(1));
            int bankId = Integer.parseInt(covenantData.get(i).get(2));

            // Null check for bannedState as there could be facility with no ban in any state
            String bannedState = "";
            try {
                bannedState = covenantData.get(i).get(3);
            } catch(Exception e) {
                bannedState = "ABCDEFGHI"; // Assigning a dummy value as there is no state that has state code as ABCDEFGHI
            }

            // if a covenant is all for facilities of a bank we add all those facilities to map by adding to the existing one
            // so, adding covenant for each facility that bank serves
            if (allFacilities) {
                for (Facility facility: facilityList) {
                    if (facility.getBankId() == bankId) { // add this facility id
                        if (!facilityCovenantMap.containsKey(facility.getId())) {
                            facilityCovenantMap.put(facility.getId(), new ArrayList<Covenant>());
                        }
                        // add covenant to the list of covenants for a specific facility id
                        facilityCovenantMap.get(facility.getId()).add(new Covenant(facility.getId(), maxDefaultLikelihood, bankId, bannedState));
                    }
                }
            } else { // if facilityId is provided
                if ( !facilityCovenantMap.containsKey(Integer.parseInt(covenantData.get(i).get(0)))) {
                    facilityCovenantMap.put(Integer.parseInt(covenantData.get(i).get(0)), new ArrayList<Covenant>());
                }
                facilityCovenantMap.get(Integer.parseInt(covenantData.get(i).get(0))).add(new Covenant(Integer.parseInt(covenantData.get(i).get(0)), maxDefaultLikelihood, bankId, bannedState));
            }

        }

    }

    private double calculateYieldPerLoan(Loan loan, Facility facility){
        Double defaultLikeHood = loan.getDefaultLikeHood();
        Long amount = loan.getAmount();
        return (1 - defaultLikeHood) * loan.getInterestRate() * amount - (defaultLikeHood * amount) - (facility.getInterestRate() * amount);
    }


    public Map<Integer,Double> generateTotalYield(Map<Integer,Integer> loanFacilityMap) {

        Map<Integer, Double> yieldMap = new HashMap<>();

        Map<Integer, Facility> facilityMap = new HashMap<Integer, Facility>();
        for(Facility facility: facilityList) {
            facilityMap.put(facility.getId(), facility); // facilityId is unique
        }

        for(Integer key : loanFacilityMap.keySet()){
            yieldMap.put(loanFacilityMap.get(key), (yieldMap.getOrDefault(loanFacilityMap.get(key), 0.0) +
                    calculateYieldPerLoan(loanMap.get(key), facilityMap.get(loanFacilityMap.get(key)))));
        }

        Map<Integer,Integer> yieldMapToNearestCent = new HashMap<>();
        for (Map.Entry<Integer, Double> yieldRecord: yieldMap.entrySet() ) {
            yieldMapToNearestCent.put(yieldRecord.getKey(),(int) Math.round(yieldRecord.getValue()));
        }

        csvFormatter.generateCSVFile(OUTPUT_YIELD_FILE_PATH, yieldMapToNearestCent, "facility_id", "expected_yield");
        return yieldMap;

    }


}
