package com.example.multidatacommunity;

import java.io.File;
import java.nio.file.Files;
import java.util.Base64;

public class ImageUtil {
    public static String encodeToBase64(File file) {
        try {
            byte[] data = Files.readAllBytes(file.toPath());
            return Base64.getEncoder().encodeToString(data);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}