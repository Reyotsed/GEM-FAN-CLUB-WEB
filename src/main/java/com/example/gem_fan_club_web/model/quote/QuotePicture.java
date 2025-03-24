package com.example.gem_fan_club_web.model.quote;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "quote_picture_info")
public class QuotePicture {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int pictureId;

    private String filePath;
}
