package com.example.nbajaj;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class DestinationHashGenerator {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java -jar DestinationHashGenerator.jar <PRN Number> <Path to JSON File>");
            return;
        }

        String prnNumber = args[0].toLowerCase();
        String jsonFilePath = args[1];

        try {
            File jsonFile = new File(jsonFilePath);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonFile);

            String destinationValue = findDestination(rootNode);
            if (destinationValue == null) {
                System.out.println("No 'destination' key found in the JSON file.");
                return;
            }

            String randomString = generateRandomString(8);
            String concatenatedString = prnNumber + destinationValue + randomString;
            String hash = generateMD5Hash(concatenatedString);
            System.out.println(hash + ";" + randomString);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String findDestination(JsonNode node) {
        if (node.isObject()) {
            for (JsonNode child : node) {
                if (child.has("destination")) {
                    return child.get("destination").asText();
                } else {
                    String result = findDestination(child);
                    if (result != null) {
                        return result;
                    }
                }
            }
        } else if (node.isArray()) {
            for (JsonNode child : node) {
                String result = findDestination(child);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    private static String generateRandomString(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }

        return sb.toString();
    }

    private static String generateMD5Hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());

            StringBuilder sb = new StringBuilder();
            for (byte b : messageDigest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
