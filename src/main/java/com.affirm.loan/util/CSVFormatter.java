package com.affirm.loan.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class CSVFormatter {

    public List<List<String>> getRecords(String fileName){

        log.info("Reading the input file - {}", fileName);
        List<List<String>> records = new ArrayList<>();
        try {
            File file = new File(fileName);
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line = "";
            while((line = br.readLine()) != null){
                records.add(parseEachLine(line));
            }
            br.close();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
        return records;
    }

    private List<String> parseEachLine(String line) {
        List<String> values = new ArrayList<>();
        String[] stringArray = line.split(",");
        for(String s: stringArray) {
            values.add(s);
        }
        return values;
    }


    public void generateCSVFile(String filepath, Map<Integer, Integer> data, String key, String value)
    {

        try(FileWriter fileWriter = new FileWriter(new File(filepath))){

            fileWriter.append(key).append(",").append(value).append(System.lineSeparator());
            for(Integer mapKey: data.keySet()){
                fileWriter.append(String.valueOf(mapKey)).append(",").append(String.valueOf(data.get(mapKey)));
                fileWriter.append(System.lineSeparator());
            }
        } catch (IOException e) {
            log.error("Failed to create the output csv file - {}", filepath);
        }

    }

}
