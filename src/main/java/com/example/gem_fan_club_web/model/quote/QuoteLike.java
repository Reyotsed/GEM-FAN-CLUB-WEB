package com.example.gem_fan_club_web.model.quote;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Entity
@Data
@Table(name = "quote_like")
public class QuoteLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private long quoteId;

    private String userId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;

    boolean isActive;

    public QuoteLike(Long quoteId, String userId) {
        this.quoteId = quoteId;
        this.userId = userId;
        this.createdAt = new Date();
        this.isActive = true;
    }

    public QuoteLike() {

    }
}
