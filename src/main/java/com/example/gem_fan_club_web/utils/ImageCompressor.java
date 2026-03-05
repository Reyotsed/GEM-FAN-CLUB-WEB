package com.example.gem_fan_club_web.utils;

import lombok.extern.slf4j.Slf4j;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

/**
 * 图片压缩工具：缩放 + JPEG 质量压缩。
 * 使用 Java 原生 ImageIO，无需额外依赖。
 */
@Slf4j
public class ImageCompressor {

    /** 上传时允许的最大宽度 */
    public static final int UPLOAD_MAX_WIDTH = 1920;
    /** 上传时 JPEG 压缩质量 (0.0-1.0) */
    public static final float UPLOAD_QUALITY = 0.82f;

    /**
     * 按指定宽度等比缩放图片，并以 JPEG 格式压缩输出。
     *
     * @param inputFile  原始图片文件
     * @param targetWidth 目标宽度，原图宽度小于此值则不放大
     * @param quality     JPEG 压缩质量 0.0 ~ 1.0
     * @return 压缩后的字节数组
     */
    public static byte[] resizeAndCompress(File inputFile, int targetWidth, float quality) throws IOException {
        BufferedImage originalImage = ImageIO.read(inputFile);
        if (originalImage == null) {
            // 无法解析的格式，直接返回原始字节
            return Files.readAllBytes(inputFile.toPath());
        }

        int origWidth = originalImage.getWidth();
        int origHeight = originalImage.getHeight();

        // 计算缩放尺寸（不放大）
        int newWidth = origWidth;
        int newHeight = origHeight;
        if (origWidth > targetWidth) {
            newWidth = targetWidth;
            newHeight = (int) Math.round((double) origHeight * targetWidth / origWidth);
        }

        // 如果不需要缩放且原图是 JPEG，检查是否需要质量压缩
        if (newWidth == origWidth && isJpeg(inputFile) && inputFile.length() <= 500 * 1024) {
            // 原图 < 500KB 的 JPEG 不需要处理
            return Files.readAllBytes(inputFile.toPath());
        }

        // 高质量缩放
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 处理 PNG 透明背景：填充白色
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, newWidth, newHeight);
        g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g2d.dispose();

        // JPEG 压缩输出
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        if (!writers.hasNext()) {
            throw new IOException("No JPEG writer available");
        }
        ImageWriter writer = writers.next();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
            writer.setOutput(ios);
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality);
            writer.write(null, new IIOImage(resizedImage, null, null), param);
        } finally {
            writer.dispose();
        }

        byte[] result = baos.toByteArray();
        log.debug("图片压缩: {}x{} -> {}x{}, {:.0f}KB -> {:.0f}KB",
                origWidth, origHeight, newWidth, newHeight,
                (double) inputFile.length() / 1024, (double) result.length / 1024);
        return result;
    }

    /**
     * 压缩上传的图片文件（就地替换）。
     * 将大于 UPLOAD_MAX_WIDTH 的图片缩放到 1920px 宽，同时 JPEG 压缩。
     */
    public static void compressUploadedFile(File file) throws IOException {
        if (!isImage(file)) return;

        BufferedImage img = ImageIO.read(file);
        if (img == null) return;

        int origWidth = img.getWidth();
        long fileSize = file.length();

        // 原图宽度 <= 1920 且文件 < 500KB，跳过
        if (origWidth <= UPLOAD_MAX_WIDTH && fileSize <= 500 * 1024) {
            return;
        }

        byte[] compressed = resizeAndCompress(file, UPLOAD_MAX_WIDTH, UPLOAD_QUALITY);

        // 只在压缩后更小时才替换
        if (compressed.length < fileSize) {
            // 重写为 .jpg
            Path originalPath = file.toPath();
            Files.write(originalPath, compressed);
            log.info("上传图片已压缩: {} ({} KB -> {} KB)",
                    file.getName(), fileSize / 1024, compressed.length / 1024);
        }
    }

    private static boolean isImage(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg")
                || name.endsWith(".png") || name.endsWith(".bmp")
                || name.endsWith(".webp");
    }

    private static boolean isJpeg(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg");
    }
}
