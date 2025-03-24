package com.example.gem_fan_club_web.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "song_info")
@Data
public class Song {
    @Id
    private String songId;

    private String title;

    private String artist;

    private String albumId;

    private String releaseDate;

    private String duration;

    private String filePath;

    private String fileType;

    private String lyrics;

    private String coverPath;
}
