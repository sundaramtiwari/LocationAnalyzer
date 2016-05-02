package com.cleartrip.main;


public class EnumTest {

    public static void main(String[] args) {
        if (SFQueue.SOLDOUT_RATE.getLabel().equals("HFF-Soldout Rate Queue"))
            System.out.println(true);
        System.out.println("Code: " + SFQueue.SOLDOUT_RATE.getCode() + " Label: " + SFQueue.SOLDOUT_RATE.getLabel());

    }

}
