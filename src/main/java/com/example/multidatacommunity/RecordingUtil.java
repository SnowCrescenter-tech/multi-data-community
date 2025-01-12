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
    // 音频文件相关常量定义
    private static final String TEMP_FOLDER = "temp/";              // 临时文件夹路径
    private static final String RECORDING_WAV = TEMP_FOLDER + "recording.wav";  // WAV临时文件
    private static final String RECORDING_MP3 = TEMP_FOLDER + "recording.mp3";  // MP3临时文件
    private static TargetDataLine line;         // 音频录制线程
    private static Thread recordingThread;       // 录音线程
    private static String base64String;         // 音频Base64编码

    /**
     * 开始录音
     * 实现步骤：
     * 1. 创建临时文件夹用于存储音频文件
     * 2. 配置音频格式(44.1kHz采样率，16位深度，单声道)
     * 3. 初始化音频捕获设备
     * 4. 创建新线程进行录音，避免阻塞主线程
     * 5. 通过缓冲区持续读取音频数据
     */
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

            // 创建录音线程
            recordingThread = new Thread(() -> {
                try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                    byte[] buffer = new byte[4096];  // 4KB缓冲区
                    while (line.isOpen()) {
                        // 从音频输入设备读取数据
                        int bytesRead = line.read(buffer, 0, buffer.length);
                        // 将读取的数据写入字节数组输出流
                        out.write(buffer, 0, bytesRead);
                    }
                    // 将录制的音频数据转换为WAV格式
                    byte[] audioData = out.toByteArray();
                    AudioInputStream ais = new AudioInputStream(
                            new ByteArrayInputStream(audioData), 
                            format,  // 音频格式：44.1kHz, 16-bit, 单声道
                            audioData.length / format.getFrameSize()  // 计算音频帧数
                    );
                    // 保存为WAV文件
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

    /**
     * 停止录音并转换格式
     * - 停止录音线程
     * - 将WAV转换为MP3
     * - 生成Base64编码
     */
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
     * 将WAV音频转换为MP3格式
     * 实现步骤：
     * 1. 初始化FFmpeg转码器
     * 2. 配置转换参数：
     *    - 使用LAME编码器
     *    - 设置比特率128kbps
     *    - 保持单声道
     *    - 保持44.1kHz采样率
     * 3. 执行转换过程
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

    /**
     * 播放Base64编码的音频
     * 实现步骤：
     * 1. 将Base64字符串解码为字节数组
     * 2. 创建音频输入流
     * 3. 获取音频格式信息
     * 4. 初始化音频播放设备
     * 5. 通过缓冲区写入音频数据进行播放
     */
    public static void playBase64Audio(String base64Audio) {
        try {
            byte[] audioData = Base64.getDecoder().decode(base64Audio);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(new ByteArrayInputStream(audioData));
            AudioFormat format = audioStream.getFormat();
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = audioStream.read(buffer, 0, buffer.length)) != -1) {
                line.write(buffer, 0, bytesRead);
            }

            line.drain();
            line.close();
            audioStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}