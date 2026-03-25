package com.example.gem_fan_club_web.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "story_info")
@Data
public class Story {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ig_media_id", nullable = false, unique = true, length = 64)
    private String igMediaId;

    @Column(name = "media_type", nullable = false)
    private Integer mediaType; // 1=图片, 2=视频

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "thumb_path")
    private String thumbPath;

    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    @Column(name = "taken_at", nullable = false)
    private LocalDateTime takenAt;

    @Column(name = "synced_at", nullable = false)
    private LocalDateTime syncedAt;
}
