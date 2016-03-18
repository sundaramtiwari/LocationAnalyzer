package com.cleartrip.main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

import com.cleartrip.dao.LocalityDao;
import com.cleartrip.entity.Locality;
import com.cleartrip.util.RestUtil;

public class LocalityLocationAnalyzer {

    private static final String NEW_LINE = "\n";
    private static final String COMMA = ",";
    private static final String HASH = "#";

    private static final String QUERY = "UPDATE PLACES.LOCALITY SET LATITUDE = #, LONGITUDE = # WHERE id = #;";
    private static final String URL = "https://maps.googleapis.com/maps/api/geocode/json?key=apiKey&address=%22pAddress%22";
    private static final String[] apiKeys = {"AIzaSyBICejdTstEMJamaR3_1yVvohjIhLikbCM", "AIzaSyA-eEugJ47AbtVKtEKR76UnLSmhQK496kM",
        "AIzaSyAy5dZojLZtH1QvVnGmq_Cv5-k37n_-Cvc", "AIzaSyAgdbTy3U8duUZIZPkfMNhMX1CeYJ_DsPU", "AIzaSyBVCNB6wuGVdNfaAGa3wQhn_UNYCl-3dgE"};

    private static final String updateFileName = "/home/sundaramtiwari/Documents/Work/LocalityUpdates.txt";
    private static final String conflictFileName = "/home/sundaramtiwari/Documents/Work/LocalityConflicts.txt";
    private static final String mulGoogleDataFileName = "/home/sundaramtiwari/Documents/Work/LocalityMultipleGoogleData.txt";

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {

        /** Get current data from DB */
        LocalityDao localityDao = new LocalityDao();
        Map<Long, Locality> localityMap = localityDao.getLocalityMap();
        
        System.out.println("Size: " + localityMap.size());

        BufferedWriter updateBufWriter = null, confilctBufWriter = null, mulGoogleDataBufWriter = null;
        FileWriter updateFileWriter = getFileWriter(updateFileName);
        FileWriter conflictFileWriter = getFileWriter(conflictFileName);
        FileWriter mulGoogleDataFileWriter = getFileWriter(mulGoogleDataFileName);

        updateBufWriter = new BufferedWriter(updateFileWriter);
        confilctBufWriter = new BufferedWriter(conflictFileWriter);
        mulGoogleDataBufWriter = new BufferedWriter(mulGoogleDataFileWriter);

        int requestCounter = 0, apiKeyIndex = 0;
        try {
            for (long localityId : localityMap.keySet()) {
                Locality locality = localityMap.get(localityId);
                System.out.println("Started " + locality.getAddress());

                /** Get corresponding lat lng data from Google */
                if (requestCounter >= 1500) {
                    requestCounter = 0;
                    apiKeyIndex++;
                } else {
                    requestCounter++;
                }
                String address = URLEncoder.encode(locality.getAddress(), "UTF-8");
                String url = URL.replaceFirst("pAddress", address);
                url = url.replaceFirst("apiKey", apiKeys[apiKeyIndex]);
                String response = RestUtil.get(url);

                try {
                    ObjectMapper objectMapper = getObjectMapper();
                    Map<String, Object> responseMap = (Map<String, Object>) objectMapper.readValue(response, Map.class);
                    List<Map<String, Object>> listOfResults = (List<Map<String, Object>>) responseMap.get("results");

                    if (listOfResults != null) {
                        if (listOfResults.size() == 1) {
                            Map<String, Object> firstResult = listOfResults.get(0);
                            Map<String, Map<String, Double>> geometryMap = (Map<String, Map<String, Double>>) firstResult.get("geometry");
                            Map<String, Double> locationMap = geometryMap.get("location");
                            double lat = locationMap.get("lat");
                            double lng = locationMap.get("lng");

                            // If locality lat lng data is in our DB, push update or conflict data
                            if (locality.getLatitude() != 0 && locality.getLongitude() != 0 && lat != 0 && lng != 0) {
                                Double dist = getLatLongDistanceInMeters(locality.getLatitude(), locality.getLongitude(), lat, lng);

                                // If deviation < 2000, push Update Statement
                                if (dist != null && dist <= 2000) {
                                    writeToUpdateFile(updateBufWriter, locality, lat, lng);
                                } else if (dist != null && dist > 2000) {
                                    // Deviation > 2000, push to conflict file
                                    writeToConflictFile(confilctBufWriter, locality, lat, lng);
                                }
                            } else {
                                // If locality lat lng data is not in our DB, push Update statement directly
                                writeToUpdateFile(updateBufWriter, locality, lat, lng);
                            }
                        }
                        // If size of listOfComponents > 1, log it to a file
                        else {
                            writeToMulGoogleDataFile(mulGoogleDataBufWriter, locality, listOfResults);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
                updateBufWriter.flush();
                confilctBufWriter.flush();
                mulGoogleDataBufWriter.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                updateBufWriter.close();
                confilctBufWriter.close();
                mulGoogleDataBufWriter.close();

                updateFileWriter.close();
                conflictFileWriter.close();
                mulGoogleDataFileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void writeToMulGoogleDataFile(BufferedWriter bw, Locality locality, List<Map<String, Object>> listOfResults) {

        for (int i = 0; i < listOfResults.size(); i++) {
            Map<String, Object> componentMap = listOfResults.get(i);
            Map<String, Map<String, Double>> geometryMap = (Map<String, Map<String, Double>>) componentMap.get("geometry");
            Map<String, Double> locationMap = geometryMap.get("location");
            
            String str = locality.getId() + COMMA + locality.getAddress() + COMMA + locality.getLatitude() + COMMA + locality.getLongitude() + COMMA + locationMap.get("lat") + COMMA + locationMap.get("lng") + NEW_LINE;

            try {
                bw.write(str);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void writeToConflictFile(BufferedWriter bw, Locality locality, double lat, double lng) {        
        String str = locality.getId() + COMMA + locality.getAddress() + COMMA + locality.getLatitude() + COMMA + locality.getLongitude() + COMMA + lat + COMMA + lng + NEW_LINE;

        try {
            bw.write(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static FileWriter getFileWriter(String fileName) {
        File file = new File(fileName);
        FileWriter fw = null;
        try {
            if (!file.exists()) {

                file.createNewFile();
                fw = new FileWriter(file.getAbsoluteFile());

            } else {
                fw = new FileWriter(file.getAbsoluteFile());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (fw == null)
            System.exit(0);
        return fw;
    }

    private static void writeToUpdateFile(BufferedWriter bw, Locality locality, double lat, double lng) throws IOException {
        String query = QUERY;
        query = query.replaceFirst(HASH, String.valueOf(lat));
        query = query.replaceFirst(HASH, String.valueOf(lng));
        query = query.replaceFirst(HASH, String.valueOf(locality.getId()));
        query += NEW_LINE;
        bw.write(query);
    }

    private static ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationConfig.Feature.WRITE_NULL_PROPERTIES, false);
        return objectMapper;
    }

    public static Double getLatLongDistanceInMeters(Double lat1, Double lng1, Double lat2, Double lng2) {
        Double dist = null;
        try {
            if (isValidLatLng(lat1, lng1, lat2, lng2)) {
                double earthRadius = 6371000; // meters
                double dLat = Math.toRadians(lat2 - lat1);
                double dLng = Math.toRadians(lng2 - lng1);
                double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
                double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
                dist = earthRadius * c;
            }
        } catch (Exception e) {
            dist = null;
        }
        return dist;
    }

    private static boolean isValidLatLng(Double lat1, Double lng1, Double lat2, Double lng2) {
        return (lat1 != null && lat1 > 0) && (lat2 != null && lat2 > 0) && (lng1 != null && lng1 > 0) && (lng2 != null && lng2 > 0);
    }
}
