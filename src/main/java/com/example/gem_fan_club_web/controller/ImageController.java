package com.example.gem_fan_club_web.controller;

import com.example.gem_fan_club_web.utils.ImageCompressor;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/image")
public class ImageController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * 缩略图磁盘缓存目录名（在 uploadDir 下自动创建）
     */
    private static final String THUMB_CACHE_DIR = "_thumbnails";

    private String getContentTypeByExtension(String filename) {
        if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (filename.endsWith(".png")) {
            return "image/png";
        } else if (filename.endsWith(".gif")) {
            return "image/gif";
        } else if (filename.endsWith(".bmp")) {
            return "image/bmp";
        } else if (filename.endsWith(".webp")) {
            return "image/webp";
        } else {
            return "application/octet-stream";
        }
    }

    /**
     * 获取图片。
     * - 不传 w 参数：返回原图（保持原有行为）。
     * - 传 w=400 等：返回等比缩放到指定宽度的 JPEG 压缩图，画质 0.80。
     *   缩略图会缓存到磁盘，同一尺寸只压缩一次。
     */
    @GetMapping("/getImageByPath")
    public void getImageByPath(
            @RequestParam String path,
            @RequestParam(value = "w", required = false) Integer width,
            HttpServletResponse response) {
        try {
            Path filePath = Paths.get(uploadDir, path);
            if (!Files.exists(filePath)) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // 如果请求了缩略图
            if (width != null && width > 0 && width < 4000) {
                serveThumbnail(filePath, path, width, response);
                return;
            }

            // 原图
            response.setContentType(getContentTypeByExtension(filePath.getFileName().toString()));
            response.setHeader("Content-Length", String.valueOf(Files.size(filePath)));
            response.setHeader("Cache-Control", "public, max-age=604800, immutable");

            try {
                Files.copy(filePath, response.getOutputStream());
            } catch (IOException e) {
                if (!e.getMessage().contains("Broken pipe")) {
                    throw e;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 生成并返回缩略图，使用磁盘缓存避免重复压缩。
     */
    private void serveThumbnail(Path originalPath, String relativePath, int width, HttpServletResponse response)
            throws IOException {
        // 缩略图缓存路径：uploadDir/_thumbnails/w400/原文件名.jpg
        String thumbFileName = stripExtension(originalPath.getFileName().toString()) + ".jpg";
        Path thumbDir = Paths.get(uploadDir, THUMB_CACHE_DIR, "w" + width);
        Path thumbPath = thumbDir.resolve(thumbFileName);

        // 如果缓存不存在，生成缩略图
        if (!Files.exists(thumbPath)) {
            Files.createDirectories(thumbDir);
            byte[] compressed = ImageCompressor.resizeAndCompress(
                    originalPath.toFile(), width, 0.80f);
            Files.write(thumbPath, compressed);
        }

        // 返回缩略图
        response.setContentType("image/jpeg");
        response.setHeader("Content-Length", String.valueOf(Files.size(thumbPath)));
        response.setHeader("Cache-Control", "public, max-age=604800, immutable");

        try {
            Files.copy(thumbPath, response.getOutputStream());
        } catch (IOException e) {
            if (!e.getMessage().contains("Broken pipe")) {
                throw e;
            }
        }
    }

    private String stripExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot > 0 ? filename.substring(0, dot) : filename;
    }
}
