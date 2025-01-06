package com.example.multidatacommunity;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Base64;

public class RecordingUtil {
    private static final String TEMP_FOLDER = "temp/";
    private static final String RECORDING_FILE = TEMP_FOLDER + "recording.wav";

    public static File recordAudio() {
        try {
            File tempFolder = new File(TEMP_FOLDER);
            if (!tempFolder.exists()) {
                tempFolder.mkdirs();
            }

            AudioFormat format = new AudioFormat(16000, 16, 1, true, true);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            if (!AudioSystem.isLineSupported(info)) {
                System.out.println("录音设备不可用");
                return null;
            }

            TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            System.out.println("开始录音...");
            line.start();

            Thread stopper = new Thread(() -> {
                try {
                    Thread.sleep(5000); // 录音5秒
                    line.stop();
                    line.close();
                    System.out.println("录音结束");

                    File file = new File(RECORDING_FILE);
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        fos.write(out.toByteArray());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            stopper.start();

            byte[] buffer = new byte[1024];
            while (line.isOpen()) {
                int bytesRead = line.read(buffer, 0, buffer.length);
                out.write(buffer, 0, bytesRead);
            }
            return new File(RECORDING_FILE);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String encodeToBase64(File file) {
        try {
            byte[] data = java.nio.file.Files.readAllBytes(file.toPath());
            return Base64.getEncoder().encodeToString(data);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}