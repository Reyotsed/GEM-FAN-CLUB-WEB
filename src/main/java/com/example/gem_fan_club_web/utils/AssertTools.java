package com.example.gem_fan_club_web.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
@Component
public class AssertTools {

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

    public void getAssert (HttpServletResponse response, String assertPath, String contentType ){
        try {
            File file = new File(assertPath);
            if (file.exists()) {

                response.setContentType(contentType);
                FileInputStream in = new FileInputStream(file);
                OutputStream out = response.getOutputStream();
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                in.close();
                out.flush();
            } else {
                response.setStatus(404);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
