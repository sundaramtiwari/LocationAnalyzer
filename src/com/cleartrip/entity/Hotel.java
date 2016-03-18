package com.cleartrip.entity;

public class Hotel {
    private long id;
    private long taHotelId;
    private double lat;
    private double lng;
    private String name;

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public long getTaHotelId() {
        return taHotelId;
    }
    public void setTaHotelId(long taHotelId) {
        this.taHotelId = taHotelId;
    }
    public double getLat() {
        return lat;
    }
    public void setLat(double lat) {
        this.lat = lat;
    }
    public double getLng() {
        return lng;
    }
    public void setLng(double lng) {
        this.lng = lng;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
}
