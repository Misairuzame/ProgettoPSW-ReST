package com.gb.utils;

public class UtilFunctions {

    public static boolean isNumber(String num) {
        try {
            int integer = Integer.parseInt(num);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
