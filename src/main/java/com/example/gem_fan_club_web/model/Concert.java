package com.example.gem_fan_club_web.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Entity
@Table(name = "i_am_gloria")
@Data
public class Concert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(length = 20)
    private String tour_phase;
    
    @Column(length = 20)
    private String sequence_range;
    
    @Column(length = 50, nullable = false)
    private String concert_date;
    
    @Column(length = 50)
    private String country;
    
    @Column(length = 50)
    private String city;
    
    @Column(length = 200)
    private String venue;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @Column(length = 100)
    private String estimated_audience;
    
    @Column(length = 20)
    private String status;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(insertable = false, updatable = false, 
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Date created_at;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(insertable = false, updatable = false, 
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private Date updated_at;
} 