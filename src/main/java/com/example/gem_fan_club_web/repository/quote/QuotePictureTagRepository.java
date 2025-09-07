package com.example.gem_fan_club_web.repository.quote;

import com.example.gem_fan_club_web.model.quote.QuotePictureTag;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

public interface QuotePictureTagRepository extends JpaRepository<QuotePictureTag, QuotePictureTag.QuotePictureTagId> {

    @Query("SELECT t.id.pictureId FROM QuotePictureTag t WHERE t.id.quoteId = :quoteId")
    List<Integer> findPictureIdsByQuoteId(@Param("quoteId") Integer quoteId);

    @Modifying
    @Transactional
    void deleteByIdQuoteId(Integer quoteId);
}