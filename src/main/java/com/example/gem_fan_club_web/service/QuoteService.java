package com.example.gem_fan_club_web.service;

import com.example.gem_fan_club_web.model.quote.Quote;
import com.example.gem_fan_club_web.model.quote.QuoteLike;
import com.example.gem_fan_club_web.model.quote.QuotePicture;
import com.example.gem_fan_club_web.model.quote.QuotePictureTag;
import com.example.gem_fan_club_web.repository.quote.QuoteLikeRepository;
import com.example.gem_fan_club_web.repository.quote.QuotePictureRepository;
import com.example.gem_fan_club_web.repository.quote.QuotePictureTagRepository;
import com.example.gem_fan_club_web.repository.quote.QuoteRepository;
import com.example.gem_fan_club_web.utils.FileTools;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuoteService {

    private final QuoteRepository quoteRepository;
    private final QuotePictureTagRepository quotePictureTagRepository;
    private final QuotePictureRepository quotePictureInfoRepository;
    private final QuoteLikeRepository quoteLikeRepository;
    private final FileTools fileTools;

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

    /**
     * 获取更多quotes（排除已显示的）
     */
    public List<Quote> getMoreQuotes(List<Integer> displayedIds, Integer count) {
        Pageable pageable = PageRequest.of(0, count);
        if (displayedIds == null || displayedIds.isEmpty()) {
            return quoteRepository.findRandomQuotes(pageable);
        }
        return quoteRepository.findRandomQuotesExcluding(displayedIds, pageable);
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

    public List<Quote> getQuotesByUserId(String userId) {
        return quoteRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Quote getQuoteById(Long quoteId) {
        return quoteRepository.findById(quoteId).orElse(null);
    }

    /**
     * 删除语录及其相关数据
     */
    @Transactional
    public void deleteQuote(Long id) {
        // 获取语录相关的图片ID
        List<Long> pictureIds = quotePictureTagRepository.findPictureIdsByQuoteId(id);
        
        // 删除语录的点赞记录
        quoteLikeRepository.deleteByQuoteId(id);
        
        // 删除语录和图片的关联关系
        quotePictureTagRepository.deleteByIdQuoteId(id);
        
        // 删除图片记录和文件
        if (!pictureIds.isEmpty()) {
            List<QuotePicture> pictures = quotePictureInfoRepository.findAllById(pictureIds);
            for (QuotePicture picture : pictures) {
                try {
                    fileTools.deleteFile(picture.getFilePath());
                } catch (IOException e) {
                    log.error("删除图片文件失败: {}", picture.getFilePath(), e);
                }
            }
            quotePictureInfoRepository.deleteAllById(pictureIds);
        }
        
        // 删除语录
        quoteRepository.deleteById(id);
    }
}