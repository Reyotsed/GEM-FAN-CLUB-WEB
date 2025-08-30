package com.example.gem_fan_club_web.controller;

import com.example.gem_fan_club_web.dto.ResponseDTO;
import com.example.gem_fan_club_web.model.Song;
import com.example.gem_fan_club_web.service.SongService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/song")
public class SongController {

    @Autowired
    private SongService songService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @GetMapping("/sampleOneSong")
    public ResponseDTO sampleOneSong() {
        List<Song> songList = songService.getAllSongs();
//        System.out.println(songList);
        return new ResponseDTO(200,"success",songList.get(0));
    }

    @GetMapping("/getAudioByPath")
    public void getAudioByPath(@RequestParam String path, HttpServletResponse response)  {
        try {
            // package的时候加上这一句：
            String normalizedPath = path.replaceAll("\\\\", "/");

            String fullPath = Paths.get(uploadDir, normalizedPath).toString();
            File file = new File(fullPath);
            if (file.exists()) {

                response.setContentType(String.valueOf(MediaType.parseMediaType("audio/mpeg"))); // 根据图片类型设置MIME类型
                FileInputStream in = new FileInputStream(file);
                OutputStream out = response.getOutputStream();
                byte[] buffer = new byte[4096];
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
