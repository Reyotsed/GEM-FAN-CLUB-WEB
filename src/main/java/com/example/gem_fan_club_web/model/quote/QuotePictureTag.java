package com.example.gem_fan_club_web.model.quote;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;

@Entity
@Table(name = "quote_picture_tag")
public class QuotePictureTag implements Serializable {

    @EmbeddedId
    private QuotePictureTagId id;

    // Getters and Setters
    public QuotePictureTagId getId() {
        return id;
    }

    public void setId(QuotePictureTagId id) {
        this.id = id;
    }

    // 嵌套的复合主键类
    @Embeddable
    public static class QuotePictureTagId implements Serializable {
        private static final long serialVersionUID = 1L;

        @Column(name = "quote_id", nullable = false)
        private Integer quoteId;

        @Column(name = "picture_id", nullable = false)
        private Integer pictureId;

        // Getters and Setters
        public Integer getQuoteId() {
            return quoteId;
        }

        public void setQuoteId(Integer quoteId) {
            this.quoteId = quoteId;
        }

        public Integer getPictureId() {
            return pictureId;
        }

        public void setPictureId(Integer pictureId) {
            this.pictureId = pictureId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            QuotePictureTagId that = (QuotePictureTagId) o;
            return quoteId.equals(that.quoteId) && pictureId.equals(that.pictureId);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(quoteId, pictureId);
        }
    }
}