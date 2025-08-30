package com.example.gem_fan_club_web.model.quote;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "quote_picture_info")
@JsonIgnoreProperties(ignoreUnknown = true)
public class QuotePicture {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("pictureId")
    private int pictureId;

    @JsonProperty("filePath")
    private String filePath;
}
