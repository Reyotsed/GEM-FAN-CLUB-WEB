package com.example.gem_fan_club_web.service;

import com.example.gem_fan_club_web.model.Song;
import com.example.gem_fan_club_web.model.quote.Quote;
import com.example.gem_fan_club_web.model.quote.QuoteLike;
import com.example.gem_fan_club_web.model.quote.QuotePicture;
import com.example.gem_fan_club_web.model.quote.QuotePictureTag;
import com.example.gem_fan_club_web.repository.quote.QuoteLikeRepository;
import com.example.gem_fan_club_web.repository.quote.QuotePictureTagRepository;
import com.example.gem_fan_club_web.repository.quote.QuotePictureRepository;
import com.example.gem_fan_club_web.repository.quote.QuoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
@Service
public class QuoteService {

    @Autowired
    QuoteRepository quoteRepository;

    @Autowired
    private QuotePictureTagRepository quotePictureTagRepository;

    @Autowired
    private QuotePictureRepository quotePictureInfoRepository;

    @Autowired
    private QuoteLikeRepository quoteLikeRepository;

    // 根据 quoteId 查找对应的图片
    public List<QuotePicture> getPicturesByQuoteId(Long quoteId) {
        // 获取所有与该 quoteId 关联的 pictureId
        List<Long> pictureIds = quotePictureTagRepository.findPictureIdsByQuoteId(quoteId);

        // 根据 pictureIds 获取图片信息
        return quotePictureInfoRepository.findAllById(pictureIds);
    }

    // 为某个语录绑定图片
    public void addPicture(List<String> filePathList, Long quoteId) {
        for (String filePath : filePathList) {
            QuotePicture quotePicture = new QuotePicture();
            quotePicture.setFilePath(filePath);
            quotePicture = quotePictureInfoRepository.save(quotePicture);

            QuotePictureTag.QuotePictureTagId compositeKey = new QuotePictureTag.QuotePictureTagId();
            compositeKey.setQuoteId(quoteId);
            compositeKey.setPictureId((long) quotePicture.getPictureId());

            if (!quotePictureTagRepository.existsById(compositeKey)) {
                // 保存 QuotePictureTag
                QuotePictureTag quotePictureTag = new QuotePictureTag();
                quotePictureTag.setId(compositeKey);
                quotePictureTagRepository.save(quotePictureTag);
            }
        }
    }

    // 添加like
    public void addLike(Long quoteId, String userId) {
        if(quoteLikeRepository.findByQuoteIdAndUserId(quoteId,userId) == null) {
            quoteLikeRepository.save(new QuoteLike(quoteId, userId));
        }
    }

    // 删除like
    public void eraseLike(Long quoteId, String userId) {
        quoteLikeRepository.deleteByQuoteIdAndUserId(quoteId,userId);
    }

    // 查找like是否存在
    public boolean isLiked(Long quoteId, String userId) {
        return quoteLikeRepository.findByQuoteIdAndUserId(quoteId,userId) != null;
    }

    public List<Quote> getAllQuote() {
        return quoteRepository.findAll();
    }

    // 添加一条quote
    public Quote addQuote(String content, String userId) {
        Quote quote = new Quote();
        quote.setContent(content);
        quote.setUserId(userId);
        quote.setCreatedAt(new Date());
        quote.setUpdatedAt(new Date());
        quote.setLikesCount(0);
        quote.setCommentsCount(0);
        return quoteRepository.save(quote);
    }

    public Quote getQuoteById(Long quoteId) {
        return quoteRepository.findById(quoteId).orElse(null);
    }
}