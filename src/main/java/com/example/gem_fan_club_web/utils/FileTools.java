package com.example.gem_fan_club_web.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class FileTools {

    @Value("${file.upload-dir}")
    private String uploadDir;

    public String saveFile(MultipartFile file, String fullPath) throws IOException {

        File uploadPath = new File(fullPath);
        if (!uploadPath.exists()) {
            uploadPath.mkdirs();
        }
        // 生成唯一的文件名，避免文件名冲突
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFilename = UUID.randomUUID().toString() + fileExtension;

//        System.out.println(newFilename);
        fullPath = Paths.get(fullPath, newFilename).toString();
        // 保存文件到本地
        File serverFile = new File(fullPath);
        file.transferTo(serverFile);

        return newFilename;
    }

    public List<String> saveImages(List<MultipartFile> images, String folder) throws IOException {
        List<String> files = new ArrayList<>();
        for (MultipartFile file : images) {
            files.add(saveFile(file,folder));
        }
        return files;
    }

    public void deleteFile(String filePath) throws IOException {
        // 将相对路径转换为绝对路径
        String absolutePath = Paths.get(uploadDir, filePath).toString();
        log.info("尝试删除文件，完整路径: {}", absolutePath);
        File file = new File(absolutePath);
        if (file.exists()) {
            if (!file.delete()) {
                throw new IOException("无法删除文件: " + absolutePath);
            }
            log.info("文件删除成功: {}", absolutePath);
        } else {
            log.warn("文件不存在: {}", absolutePath);
        }
    }
}
