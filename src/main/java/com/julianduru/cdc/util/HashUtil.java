package com.julianduru.cdc.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;

/**
 * created by Julian Duru on 29/04/2023
 */
public class HashUtil {


    public static String hashObjectList(List<Object> objectList) {
        StringBuilder sb = new StringBuilder();

        for (Object object : objectList) {
            int objectHash = Objects.hash(object);
            sb.append(objectHash);
        }

        String concatenatedHashes = sb.toString();

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(concatenatedHashes.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();

        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        return hexString.toString();
    }


}

