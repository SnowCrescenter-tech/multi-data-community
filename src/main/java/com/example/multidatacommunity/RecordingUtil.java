package com.example.multidatacommunity;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Base64;

public class RecordingUtil {
    private static final String TEMP_FOLDER = "temp/";
    private static final String RECORDING_WAV = TEMP_FOLDER + "recording.wav";
    private static final String RECORDING_MP3 = TEMP_FOLDER + "recording.mp3";
    private static TargetDataLine line;
    private static Thread recordingThread;
    private static String base64String;

    public static void startRecording() {
        try {
            File tempFolder = new File(TEMP_FOLDER);
            if (!tempFolder.exists()) {
                tempFolder.mkdirs();
            }

            // 设置音频格式：44.1 kHz, 16-bit, 单声道
            AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            if (!AudioSystem.isLineSupported(info)) {
                System.out.println("录音设备不可用");
                return;
            }

            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();

            recordingThread = new Thread(() -> {
                try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                    byte[] buffer = new byte[4096];
                    while (line.isOpen()) {
                        int bytesRead = line.read(buffer, 0, buffer.length);
                        out.write(buffer, 0, bytesRead);
                    }
                    byte[] audioData = out.toByteArray();
                    AudioInputStream ais = new AudioInputStream(
                            new ByteArrayInputStream(audioData), format, audioData.length / format.getFrameSize()
                    );
                    AudioSystem.write(ais, AudioFileFormat.Type.WAVE, new File(RECORDING_WAV));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            recordingThread.start();
            System.out.println("开始录音...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void stopRecording() {
        File mp3File = null;
        try {
            if (line != null) {
                line.stop();
                line.close();
            }
            if (recordingThread != null) {
                recordingThread.join();
            }
            System.out.println("录音结束");

            // 转换 WAV 到 MP3
            File wavFile = new File(RECORDING_WAV);
            mp3File = new File(RECORDING_MP3);
            convertWavToMp3UsingFFmpeg(wavFile, mp3File);

            // 将 MP3 文件编码为 Base64
            base64String = encodeToBase64(mp3File);
            System.out.println("录音文件已转换为编码");
//            writeBase64ToFile(base64String);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 使用 FFmpeg 将 WAV 文件转换为 MP3 文件
     *
     * @param wavFile 输入的 WAV 文件
     * @param mp3File 输出的 MP3 文件
     * @throws IOException 如果文件操作失败
     */
    private static void convertWavToMp3UsingFFmpeg(File wavFile, File mp3File) throws IOException {
        try {
            // 初始化 FFmpeg
            FFmpeg ffmpeg = new FFmpeg(); // 默认使用系统 PATH 中的 FFmpeg
            FFmpegExecutor executor = new FFmpegExecutor(ffmpeg);

            // 构建 FFmpeg 命令
            FFmpegBuilder builder = new FFmpegBuilder()
                    .setInput(wavFile.getAbsolutePath()) // 输入文件
                    .overrideOutputFiles(true) // 覆盖输出文件
                    .addOutput(mp3File.getAbsolutePath()) // 输出文件
                    .setAudioCodec("libmp3lame") // 使用 LAME 编码器
                    .setAudioBitRate(128_000) // 设置比特率为 128 kbps
                    .setAudioChannels(1) // 单声道
                    .setAudioSampleRate(44_100) // 采样率为 44.1 kHz
                    .done();

            // 执行转换
            executor.createJob(builder).run();
        } catch (Exception e) {
            throw new IOException("FFmpeg 转换失败", e);
        }
    }

    /**
     * 将文件编码为 Base64 字符串
     *
     * @param file 要编码的文件
     * @return Base64 编码的字符串
     */
    public static String encodeToBase64(File file) {
        try {
            byte[] data = Files.readAllBytes(file.toPath());
            return Base64.getEncoder().encodeToString(data);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取 Base64 编码的 MP3 文件内容
     *
     * @return Base64 编码的字符串
     */
    public static String encodeToBase64() {
        return base64String;
    }
    public static void writeBase64ToFile(String base64String) {
        Path path = Paths.get("base.txt");
        try {
            Files.writeString(path, base64String, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("Base64 string written to base.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}