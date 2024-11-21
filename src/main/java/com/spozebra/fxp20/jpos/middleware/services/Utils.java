package com.spozebra.fxp20.jpos.middleware.services;

public class Utils {
    public static byte[] hexStringToByteArray(String sHex) {
        System.out.println(" sHex : " + sHex);
        try {

            if (sHex == null)
                throw new IllegalArgumentException();
            sHex = sHex.replaceAll("\\s", "");
            if (sHex.length() % 2 != 0)
                throw new IllegalArgumentException();
            char[] chars = sHex.toCharArray();
            int len = chars.length;
            byte[] data = new byte[len / 2];
            for (int i = 0; i < len; i += 2)
                data[i / 2] = (byte) ((Character.digit(chars[i], 16) << 4) + Character.digit(chars[i + 1], 16));
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }
}
