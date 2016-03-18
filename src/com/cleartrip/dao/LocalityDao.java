package com.cleartrip.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import com.cleartrip.entity.Hotel;
import com.cleartrip.entity.Locality;

public class LocalityDao {

    // private static final String url = "jdbc:oracle:thin:@172.16.63.25:1521:CTDBQA2";
    private static final String url = "jdbc:oracle:thin:@ctoradb.cleartrip.com:1521/cleardb";
    // private static final String url = "jdbc:oracle:thin:@ctoradb.cleartrip.com:1521/cleardb3";

    private static final String username = "ctb";
    private static final String password = "ctb";

    private static final String fetchLocalitiesQuery = "select l.id, l.locality_name, ci.city_name, l.longitude, l.latitude from locality l, city ci, country co "
            + "where l.city_id = ci.id and ci.COUNTRY_ID = co.id and co.COUNTRY_NAME=\'India\'";

    private static final String fetchHotelsQuery = "select a.HOTEL_ID, b.hotel_name, a.TA_HOTEL_ID, c.LATITUDE, c.LONGITUDE "
            + "from hotel_rating_map a, hotel b, hotel_location_info c, city ci, country co " 
            + "where a.hotel_id = b.id and b.id = c.HOTEL_ID and c.CITY_ID = ci.id and ci.COUNTRY_ID = co.id and co.COUNTRY_NAME=\'India\'";

    public Map<Long, Locality> getLocalityMap() {
        Map<Long, Locality> localityMap = new HashMap<Long, Locality>();
        Connection con = JdbcFactory.getConnection(url, username, password);
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            System.out.println("Getting locality details from DB...");
            ResultSet rs = stmt.executeQuery(fetchLocalitiesQuery);
            while (rs.next()) {
                updateLocalityMap(localityMap, rs);
            }
            System.out.println("Locality details completed. ");
            return localityMap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return localityMap;
    }

    private static void updateLocalityMap(Map<Long, Locality> localityMap, ResultSet rs) {
        long id = 0;
        String lName = null, city = null, address = null;
        Locality locality = new Locality();
        try {
            if (rs.getObject("ID") != null) {
                id = rs.getLong("ID");
                locality.setId(id);
            }
            if (rs.getObject("locality_name") != null) {
                lName = rs.getString("locality_name");
                locality.setName(lName);
            }
            if (rs.getObject("city_name") != null) {
                city = rs.getString("city_name");
                locality.setCity(city);
            }
            if (rs.getObject("longitude") != null) {
                locality.setLongitude(rs.getDouble("longitude"));
            }
            if (rs.getObject("latitude") != null) {
                locality.setLatitude(rs.getDouble("latitude"));
            }
            if (lName != null && city != null) {
                address = lName + "," + city;
                locality.setAddress(address);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        localityMap.put(id, locality);
    }

    public Map<Long, Hotel> getLocalityMapForHotels() {
        Map<Long, Hotel> hotelMap = new HashMap<Long, Hotel>();
        Connection con = JdbcFactory.getConnection(url, username, password);
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            System.out.println("Getting hotel details from DB...");
            ResultSet rs = stmt.executeQuery(fetchHotelsQuery);
            while (rs.next()) {
                updateHotelMap(hotelMap, rs);
            }
            System.out.println("Hotel details completed. ");
            return hotelMap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hotelMap;
    }

    private void updateHotelMap(Map<Long, Hotel> hotelMap, ResultSet rs) {
        long id = 0, taId = 0;
        Hotel hotel = new Hotel();
        try {
            if (rs.getObject("HOTEL_ID") != null) {
                id = rs.getLong("HOTEL_ID");
                hotel.setId(id);
            }
            if (rs.getObject("TA_HOTEL_ID") != null) {
                String taIdStr = rs.getString("TA_HOTEL_ID");
                if (isNumeric(taIdStr)) {
                    try {
                        taId = Long.valueOf(taIdStr);
                        hotel.setTaHotelId(taId);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            if (rs.getObject("LONGITUDE") != null) {
                hotel.setLng(rs.getDouble("longitude"));
            }
            if (rs.getObject("LATITUDE") != null) {
                hotel.setLat(rs.getDouble("latitude"));
            }
            if (rs.getObject("hotel_name") != null) {
                hotel.setName(rs.getString("hotel_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        hotelMap.put(id, hotel);
    }
    
    public static boolean isNumeric(String str)
    {
        return str.matches("[+-]?\\d*?");
    }
}
