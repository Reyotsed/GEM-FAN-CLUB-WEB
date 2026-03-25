package com.example.gem_fan_club_web.controller;

import com.example.gem_fan_club_web.dto.ResponseDTO;
import com.example.gem_fan_club_web.model.Story;
import com.example.gem_fan_club_web.service.StoryService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/story")
public class StoryController {

    @Autowired
    private StoryService storyService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * Python 脚本调用：同步单条快拍
     */
    @PostMapping("/sync")
    public ResponseDTO syncStory(
            @RequestParam("file") MultipartFile file,
            @RequestParam("igMediaId") String igMediaId,
            @RequestParam("mediaType") Integer mediaType,
            @RequestParam("takenAt") Long takenAt,
            @RequestParam(value = "width", required = false) Integer width,
            @RequestParam(value = "height", required = false) Integer height) {
        try {
            Story story = storyService.syncStory(file, igMediaId, mediaType, takenAt, width, height);
            if (story == null) {
                return new ResponseDTO(200, "already_exists", null);
            }
            Map<String, Object> result = new HashMap<>();
            result.put("id", story.getId());
            result.put("igMediaId", story.getIgMediaId());
            result.put("filePath", story.getFilePath());
            return new ResponseDTO(200, "success", result);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseDTO(500, "sync failed: " + e.getMessage(), null);
        }
    }

    /**
     * 前端调用：分页查询快拍
     */
    @GetMapping("/list")
    public ResponseDTO getStoryList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        Page<Story> storyPage = storyService.getStoryList(page, size);
        return new ResponseDTO(200, "success", storyPage);
    }

    /**
     * 前端调用：返回快拍媒体文件（图片/视频流式传输）
     */
    @GetMapping("/media")
    public void getMediaByPath(@RequestParam String path, HttpServletResponse response) {
        try {
            Path filePath = Paths.get(uploadDir, path);
            if (!Files.exists(filePath)) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            String filename = filePath.getFileName().toString().toLowerCase();
            if (filename.endsWith(".mp4")) {
                response.setContentType("video/mp4");
            } else if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
                response.setContentType("image/jpeg");
            } else if (filename.endsWith(".png")) {
                response.setContentType("image/png");
            } else if (filename.endsWith(".webp")) {
                response.setContentType("image/webp");
            } else {
                response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            }

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
}
