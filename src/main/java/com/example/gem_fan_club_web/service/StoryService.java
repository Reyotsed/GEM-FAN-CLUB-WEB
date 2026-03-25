package com.example.gem_fan_club_web.service;

import com.example.gem_fan_club_web.model.Story;
import com.example.gem_fan_club_web.repository.StoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Service
public class StoryService {

    @Autowired
    private StoryRepository storyRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    private static final String STORY_DIR = "stories";

    /**
     * 同步单条快拍：去重 → 保存文件 → 入库
     *
     * @return 保存后的 Story 实体；若已存在返回 null
     */
    public Story syncStory(MultipartFile file, String igMediaId, Integer mediaType,
                           Long takenAtTimestamp, Integer width, Integer height) throws IOException {
        // 1. 去重检查
        if (storyRepository.existsByIgMediaId(igMediaId)) {
            log.info("快拍已存在，跳过: igMediaId={}", igMediaId);
            return null;
        }

        // 2. 确保存储目录存在
        String storyDirPath = Paths.get(uploadDir, STORY_DIR).toString();
        File dir = new File(storyDirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 3. 保存文件（使用原始文件名，因为 Python 端已命名好）
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            String ext = mediaType == 2 ? ".mp4" : ".jpg";
            originalFilename = igMediaId + ext;
        }
        String fullPath = Paths.get(storyDirPath, originalFilename).toString();
        File targetFile = new File(fullPath);
        file.transferTo(targetFile);
        log.info("快拍文件已保存: {}", fullPath);

        // 4. 构建实体并入库
        Story story = new Story();
        story.setIgMediaId(igMediaId);
        story.setMediaType(mediaType);
        story.setFilePath(STORY_DIR + "/" + originalFilename);
        story.setWidth(width);
        story.setHeight(height);
        story.setTakenAt(LocalDateTime.ofInstant(
                Instant.ofEpochSecond(takenAtTimestamp), ZoneId.of("Asia/Shanghai")));
        story.setSyncedAt(LocalDateTime.now());

        return storyRepository.save(story);
    }

    /**
     * 分页查询快拍（按发布时间倒序）
     */
    public Page<Story> getStoryList(int page, int size) {
        return storyRepository.findAllByOrderByTakenAtDesc(PageRequest.of(page, size));
    }
}
