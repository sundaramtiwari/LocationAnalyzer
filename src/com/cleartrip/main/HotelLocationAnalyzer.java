package com.cleartrip.main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

import com.cleartrip.dao.LocalityDao;
import com.cleartrip.entity.Hotel;
import com.cleartrip.util.RestUtil;

public class HotelLocationAnalyzer {

    private static final String NEW_LINE = "\n";
    private static final String COMMA = ",";
    private static final String HASH = "#";

    private static final String updateFileName = "/home/sundaramtiwari/Documents/Work/HotelUpdates.txt";
    private static final String conflictFileName = "/home/sundaramtiwari/Documents/Work/HotelConflicts.txt";
    private static final String noTaIdHotelFileName = "/home/sundaramtiwari/Documents/Work/HotelNoTAId.txt";

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {

        /** Get current data from DB */
        LocalityDao hotelDao = new LocalityDao();
        Map<Long, Hotel> hotelMap = hotelDao.getLocalityMapForHotels();

        BufferedWriter updateBufWriter = null, confilctBufWriter = null, noTaIdHotelBufWriter = null;
        FileWriter updateFileWriter = getFileWriter(updateFileName);
        FileWriter conflictFileWriter = getFileWriter(conflictFileName);
        FileWriter noTaIdHotelFileWriter = getFileWriter(noTaIdHotelFileName);

        updateBufWriter = new BufferedWriter(updateFileWriter);
        confilctBufWriter = new BufferedWriter(conflictFileWriter);
        noTaIdHotelBufWriter = new BufferedWriter(noTaIdHotelFileWriter);

        int count = 0;
        try {
            for (long hotelId : hotelMap.keySet()) {
                Hotel hotel = hotelMap.get(hotelId);
                if (count >= 100)
                    break;
                else {
                    count++;
                }
                if (hotel.getTaHotelId() != 0) {
                    /** Get corresponding lat lng data from TA */
                    String url = "http://api.tripadvisor.com/api/partner/2.0/location/hotelId/hotels?key=DCB4CB6C2EAC432C8E84471D616CAD93";
                    url = url.replaceFirst("hotelId", String.valueOf(hotel.getTaHotelId()));
                    System.out.println(url);
                    String response = RestUtil.get(url);
                    
                    try {
                        ObjectMapper objectMapper = getObjectMapper();
                        Map<String, Object> responseMap = (Map<String, Object>) objectMapper.readValue(response, Map.class);
                        List<Map<String, String>> data = (List<Map<String, String>>) responseMap.get("data");

                        if (data != null) {
                            Map<String, String> firstResult = data.get(0);
                            double lat = Double.valueOf(firstResult.get("latitude"));
                            double lng = Double.valueOf(firstResult.get("longitude"));

                            /** If hotel lat lng data is in our DB, push to update or conflict data */
                            if (hotel.getLat() != 0 && hotel.getLng() != 0 && lat != 0 && lng != 0) {
                                Double dist = getLatLongDistanceInMeters(hotel.getLat(), hotel.getLng(), lat, lng);

                                // If deviation < 500, push Update Statement
                                if (dist != null && dist <= 500) {
                                    writeToUpdateFile(updateBufWriter, hotel, lat, lng);
                                } else if (dist != null && dist > 500) {
                                    // Deviation > 500, push to conflict file
                                    writeToConflictFile(confilctBufWriter, hotel, lat, lng);
                                }
                            } else {
                                // If hotel lat lng data is not in our DB, push Update statement directly
                                writeToUpdateFile(updateBufWriter, hotel, lat, lng);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    writeToHotelNoTAFile(noTaIdHotelBufWriter, hotel);
                }
                updateBufWriter.flush();
                confilctBufWriter.flush();
                noTaIdHotelBufWriter.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                updateBufWriter.close();
                confilctBufWriter.close();

                updateFileWriter.close();
                conflictFileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void writeToHotelNoTAFile(BufferedWriter bw, Hotel hotel) {
        String str = hotel.getId() + COMMA + hotel.getName() + COMMA + hotel.getLat() + COMMA + hotel.getLng() + NEW_LINE;
        try {
            bw.write(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeToConflictFile(BufferedWriter bw, Hotel hotel, double lat, double lng) {
        String str = hotel.getId() + COMMA + hotel.getName() + COMMA + hotel.getLat() + COMMA + hotel.getLng() + COMMA + lat + COMMA + lng + NEW_LINE;
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

    private static void writeToUpdateFile(BufferedWriter bw, Hotel hotel, double lat, double lng) throws IOException {
        String query = "UPDATE PLACES.hotel_location_info SET LATITUDE = #, LONGITUDE = # WHERE HOTEL_ID = #;";
        query = query.replaceFirst(HASH, String.valueOf(lat));
        query = query.replaceFirst(HASH, String.valueOf(lng));
        query = query.replaceFirst(HASH, String.valueOf(hotel.getId()));
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
