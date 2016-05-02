package com.cleartrip.main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.cleartrip.dao.LocalityDao;
import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

public class HotelLocationDetails {

    private static final String OUTPUT_FILE = "/home/sundaramtiwari/Documents/Work/HotelData/HotelData.csv";
    
    private static final String INPUT_FILE = "/home/sundaramtiwari/Documents/HotelData/TAComparisonResult.csv";

    public static void main(String[] args) {

        /** Maps containing ctHotelId as key and TAHotelId as value */
        Map<String, String> dbHotelMap = new HashMap<String, String>();
        Map<String, String> exHotelMap = new HashMap<String, String>();
        
        CsvReader reader = null;
            try {
                reader = new CsvReader(INPUT_FILE);
                reader.readRecord();

                int dbCtHotelId = 0, dbTAHotelId = 1, exCtHotelId = 2, exTAHotelId = 3;

                while (reader.readRecord()) {
                    String dbCtHotelIdVal = null, dbTAHotelIdVal = null, exCtHotelIdVal = null, exTAHotelIdVal = null;
                    try {
                        if (StringUtils.isNotBlank(reader.get(dbCtHotelId))) {
                            dbCtHotelIdVal = reader.get(dbCtHotelId);
                        }
                        if (StringUtils.isNotBlank(reader.get(dbTAHotelId))) {
                            dbTAHotelIdVal = reader.get(dbTAHotelId);
                        }
                        if (StringUtils.isNotBlank(reader.get(exCtHotelId))) {
                            exCtHotelIdVal = reader.get(exCtHotelId);
                        }
                        if (StringUtils.isNotBlank(reader.get(exTAHotelId))) {
                            exTAHotelIdVal = reader.get(exTAHotelId);
                        }
                        dbHotelMap.put(dbCtHotelIdVal, dbTAHotelIdVal);
                        exHotelMap.put(exCtHotelIdVal, exTAHotelIdVal);
                        
                    } catch (Exception e) {
                        System.out.println("Error parsing MIS record from CSV" + e);
                    }
                }
            } catch (Exception e) {
                if (reader == null) {
                    System.out.println("Error reading MIS CSV file: \n" + e);
                }
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (Exception e) {
                        System.out.println("Exception occured in closing reader: " + e);
                    }
                }
            }
        System.out.println("Completed reading!!!");
        
        for (String exHotelId : exHotelMap.keySet()) {
            String exTAHotelId = exHotelMap.get(exHotelId);
            if (dbHotelMap.containsKey(exHotelId)) {
                String dbTAHotelId = dbHotelMap.get(exHotelId);
                if (!exTAHotelId.equals(dbTAHotelId)) {
                    System.out.println("CTHotelId: " + exHotelId + " DB TAHotelId: " + dbTAHotelId + " Excel TAHotelId: " + exTAHotelId);
                }
            }
        }

        //getTAHotelDetails();
    }

    /**
     * 
     */
    private static void getTAHotelDetails() {
        LocalityDao hotelDao = new LocalityDao();
        Map<Long, Long> hotelMap = hotelDao.getTAHotelIDs();
        System.out.println("Total number of hotels: " + hotelMap.size());

        try {
            boolean alreadyExists = new File(OUTPUT_FILE).exists();
            CsvWriter csvOutput = new CsvWriter(new FileWriter(OUTPUT_FILE, true), ',');

            if (!alreadyExists) {
                csvOutput.write("CT HotelId");
                csvOutput.write("TA HotelId");
                csvOutput.endRecord();
            }

            for (Long hotelId : hotelMap.keySet()) {
                Long taHotelId = hotelMap.get(hotelId);

                csvOutput.write(String.valueOf(hotelId));
                csvOutput.write(String.valueOf(taHotelId));
                csvOutput.endRecord();
            }
            csvOutput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
