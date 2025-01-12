package com.example.multidatacommunity;

import java.io.File;
import java.nio.file.Files;
import java.util.Base64;

public class ImageUtil {
    /**
     * 将图片文件转换为Base64编码
     * @param file 要编码的图片文件
     * @return Base64编码的字符串
     */
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