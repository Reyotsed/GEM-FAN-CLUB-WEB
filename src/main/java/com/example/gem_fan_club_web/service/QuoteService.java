package com.example.gem_fan_club_web.service;

import com.example.gem_fan_club_web.model.quote.Quote;
import com.example.gem_fan_club_web.model.quote.QuoteLike;
import com.example.gem_fan_club_web.model.quote.QuotePicture;
import com.example.gem_fan_club_web.model.quote.QuotePictureTag;
import com.example.gem_fan_club_web.redis.RedisService;
import com.example.gem_fan_club_web.redis.RedisUtils.QuotePictureListWrapper;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuoteService {

    private final QuoteRepository quoteRepository;
    private final QuotePictureTagRepository quotePictureTagRepository;
    private final QuotePictureRepository quotePictureInfoRepository;
    private final QuoteLikeRepository quoteLikeRepository;
    private final FileTools fileTools;
    private final RedisService redisService;
    private final Executor asyncExecutor;

    // 根据 quoteId 查找对应的图片（逻辑过期缓存）
    public List<QuotePicture> getPicturesByQuoteId(Integer quoteId) {
        // 首先尝试从缓存获取
        QuotePictureListWrapper cachedWrapper = redisService.getQuotePictureListCache(quoteId);
        
        if (cachedWrapper != null) {
            // 缓存存在，检查是否过期
            if (cachedWrapper.isExpired()) {
                log.debug("缓存已过期，启动异步更新，quoteId: {}", quoteId);
                // 异步更新缓存，避免阻塞当前请求
                CompletableFuture.runAsync(() -> {
                    try {
                        updateQuotePictureListCache(quoteId);
                        log.debug("异步更新缓存完成，quoteId: {}", quoteId);
                    } catch (Exception e) {
                        log.error("异步更新缓存失败，quoteId: {}", quoteId, e);
                    }
                }, asyncExecutor);
            } else {
                log.debug("从缓存获取QuotePicture列表，quoteId: {}", quoteId);
            }
            // 返回缓存数据（即使过期也返回，保证可用性）
            return cachedWrapper.getData();
        }

        // 缓存未命中，从数据库获取并缓存
        log.debug("缓存未命中，从数据库获取QuotePicture列表，quoteId: {}", quoteId);
        return updateQuotePictureListCache(quoteId);
    }

    /**
     * 更新QuotePicture列表缓存
     */
    private List<QuotePicture> updateQuotePictureListCache(Integer quoteId) {
        List<Integer> pictureIds = quotePictureTagRepository.findPictureIdsByQuoteId(quoteId);
        List<QuotePicture> pictures = quotePictureInfoRepository.findAllById(pictureIds);
        
        // 将结果存入缓存（逻辑过期）
        if (!pictures.isEmpty()) {
            redisService.setQuotePictureListCache(quoteId, pictures);
            log.debug("QuotePicture列表缓存已更新，quoteId: {}", quoteId);
        }
        
        return pictures;
    }

    // 为某个语录绑定图片
    public void addPicture(List<String> filePathList, Integer quoteId) {
        for (String filePath : filePathList) {
            QuotePicture quotePicture = new QuotePicture();
            quotePicture.setFilePath(filePath);
            quotePicture = quotePictureInfoRepository.save(quotePicture);

            QuotePictureTag.QuotePictureTagId compositeKey = new QuotePictureTag.QuotePictureTagId();
            compositeKey.setQuoteId(quoteId);
            compositeKey.setPictureId(quotePicture.getPictureId()); // No casting needed!

            if (!quotePictureTagRepository.existsById(compositeKey)) {
                // 保存 QuotePictureTag
                QuotePictureTag quotePictureTag = new QuotePictureTag();
                quotePictureTag.setId(compositeKey);
                quotePictureTagRepository.save(quotePictureTag);
            }
            
            // 缓存新创建的QuotePicture（逻辑过期）
            redisService.setQuotePictureCache(quotePicture.getPictureId(), quotePicture);
        }
        
        // 清除相关的列表缓存，因为数据已更新
        redisService.deleteQuotePictureListCache(quoteId);
    }

    // 添加like
    public void addLike(Integer quoteId, String userId) {
        if(quoteLikeRepository.findByQuoteIdAndUserId(quoteId,userId) == null) {
            quoteLikeRepository.save(new QuoteLike(quoteId, userId));
        }
    }

    // 删除like
    public void eraseLike(Integer quoteId, String userId) {
        quoteLikeRepository.deleteByQuoteIdAndUserId(quoteId,userId);
    }

    // 查找like是否存在
    public boolean isLiked(Integer quoteId, String userId) {
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

    public Quote getQuoteById(Integer quoteId) {
        return quoteRepository.findById(quoteId).orElse(null);
    }

    /**
     * 删除语录及其相关数据
     */
    @Transactional
    public void deleteQuote(Integer id) {
        // 获取语录相关的图片ID
        List<Integer> pictureIds = quotePictureTagRepository.findPictureIdsByQuoteId(id);
        
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
                    // 清除相关缓存
                    redisService.deleteQuotePictureCache(picture.getPictureId());
                } catch (IOException e) {
                    log.error("删除图片文件失败: {}", picture.getFilePath(), e);
                }
            }
            quotePictureInfoRepository.deleteAllById(pictureIds);
        }
        
        // 清除相关的列表缓存
        redisService.deleteQuotePictureListCache(id);
        
        // 删除语录
        quoteRepository.deleteById(id);
    }
}