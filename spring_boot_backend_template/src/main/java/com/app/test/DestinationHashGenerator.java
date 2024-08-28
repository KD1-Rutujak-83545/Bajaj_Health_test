package com.app.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Iterator;

public class DestinationHashGenerator {
    public static void main(String[] args) {
        // Check if the correct number of arguments is provided
        if (args.length < 2) {
            System.err.println("Usage: java DestinationHashGenerator <prnNumber> <jsonFilePath>");
            System.exit(1); // Exit with an error code
        }

        try {
            // Parse arguments
            String prnNumber = args[0].toLowerCase();
            String jsonFilePath = args[1];

            // Parse JSON file
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(new File(jsonFilePath));
            String destinationValue = findDestination(rootNode);

            // Generate random 8-character alphanumeric string
            String randomString = generateRandomString();

            // Create the MD5 hash
            String concatenated = prnNumber + destinationValue + randomString;
            String md5Hash = generateMD5(concatenated);

            // Print the output in the required format
            System.out.println(md5Hash + ";" + randomString);
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private static String findDestination(JsonNode node) {
        Iterator<String> fieldNames = node.fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            JsonNode childNode = node.get(fieldName);
            if (fieldName.equals("destination")) {
                return childNode.asText();
            } else if (childNode.isObject() || childNode.isArray()) {
                String result = findDestination(childNode);
                if (result != null) return result;
            }
        }
        return null;
    }

    private static String generateRandomString() {
        SecureRandom random = new SecureRandom();
        return random.ints(48, 122 + 1)
                .filter(i -> Character.isLetterOrDigit(i))
                .limit(8)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    private static String generateMD5(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] hashBytes = md.digest(input.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
