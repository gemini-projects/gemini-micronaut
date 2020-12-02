package it.at7.gemini.micronaut.core;

public class Configurations {
    private static String LK_SINGLE_RECORD = "_singlerec_lk";

    public static String getLkSingleRecord() {
        return LK_SINGLE_RECORD;
    }

    public static void setLkSingleRecord(String lkSingleRecord) {
        LK_SINGLE_RECORD = lkSingleRecord;
    }
}
