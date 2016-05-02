package com.cleartrip.main;

public enum SFQueue {

    SOLDOUT_RATE("SOLDOUT_RATE", "HFF-Soldout Rate Queue"), SOLDOUT_INVENTORY("SOLDOUT_INVENTORY", "HFF-Soldout Inventory Queue"),
    PENDING_FF("PENDING_FF", "HFF-Pending FF Queue"), PENDING_BD("PENDING_BD", "HFF-Pending BD"), ALTERNATE("ALTERNATE", "HFF-Alternate Queue"),
    BOUNCED("BOUNCED", "HFF-Bounced Queue"), CONFIRMED("CONFIRMED", " HFF-Confirmed Queue");
    
    private String code;

    private String label;

    private SFQueue(String pcode, String plabel) {
        code = pcode;
        label = plabel;
    }

    public final String getCode() {
        return code;
    }

    public final String getLabel() {
        return label;
    }
}
