package com.example.gem_fan_club_web.controller;

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

@RestController
@RequestMapping("/image")
public class ImageController {

    @Value("${file.upload-dir}")
    private String uploadDir;

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
            return "application/octet-stream"; // 默认类型
        }
    }

    @GetMapping("/getImageByPath")
    public void getImageByPath(@RequestParam String path, HttpServletResponse response) {
        try {

            // package的时候加上这一句：
            String normalizedPath = path.replaceAll("\\\\", "/");
            System.out.println(uploadDir);
            Path filePath = Paths.get(uploadDir, path);
            System.out.println(filePath);
            if (!Files.exists(filePath)) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            response.setContentType(getContentTypeByExtension(filePath.getFileName().toString()));
            response.setHeader("Content-Length", String.valueOf(Files.size(filePath)));

            try (InputStream is = Files.newInputStream(filePath)) {
                Files.copy(filePath, response.getOutputStream());
            } catch (IOException e) {
                if (e.getMessage().contains("Broken pipe")) {
                } else {
                    throw e;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
