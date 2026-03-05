package com.example.gem_fan_club_web.service;

import com.example.gem_fan_club_web.dto.QuoteCardDTO;
import com.example.gem_fan_club_web.model.User;
import com.example.gem_fan_club_web.model.quote.Quote;
import com.example.gem_fan_club_web.model.quote.QuoteLike;
import com.example.gem_fan_club_web.model.quote.QuotePicture;
import com.example.gem_fan_club_web.model.quote.QuotePictureTag;
import com.example.gem_fan_club_web.redis.RedisService;
import com.example.gem_fan_club_web.redis.RedisUtils.QuotePictureListWrapper;
import com.example.gem_fan_club_web.repository.UserRepository;
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
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuoteService {

    private final QuoteRepository quoteRepository;
    private final QuotePictureTagRepository quotePictureTagRepository;
    private final QuotePictureRepository quotePictureInfoRepository;
    private final QuoteLikeRepository quoteLikeRepository;
    private final UserRepository userRepository;
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
     * 聚合接口：一次性获取多条语录的完整卡片数据（含用户信息、图片路径、点赞状态）
     * 将原先前端 N+1 次请求压缩为后端一次 DB 批量查询。
     */
    public List<QuoteCardDTO> getMoreQuoteCards(List<Integer> displayedIds, Integer count, String currentUserId) {
        Pageable pageable = PageRequest.of(0, count);
        List<Quote> quotes;
        if (displayedIds == null || displayedIds.isEmpty()) {
            quotes = quoteRepository.findRandomQuotes(pageable);
        } else {
            quotes = quoteRepository.findRandomQuotesExcluding(displayedIds, pageable);
        }

        if (quotes.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. 批量收集所有需要查询的 userId
        Set<String> userIds = quotes.stream().map(Quote::getUserId).collect(Collectors.toSet());
        Map<String, User> userMap = new HashMap<>();
        for (User u : userRepository.findAllById(userIds)) {
            userMap.put(u.getUserId(), u);
        }

        // 2. 批量查询每条语录的图片路径
        Map<Integer, List<String>> quotePictureMap = new HashMap<>();
        for (Quote q : quotes) {
            List<QuotePicture> pics = getPicturesByQuoteId(q.getQuoteId());
            List<String> paths = pics.stream().map(QuotePicture::getFilePath).collect(Collectors.toList());
            quotePictureMap.put(q.getQuoteId(), paths);
        }

        // 3. 批量查询当前用户的点赞状态
        Set<Integer> likedQuoteIds = new HashSet<>();
        if (currentUserId != null && !currentUserId.isEmpty()) {
            for (Quote q : quotes) {
                if (quoteLikeRepository.findByQuoteIdAndUserId(q.getQuoteId(), currentUserId) != null) {
                    likedQuoteIds.add(q.getQuoteId());
                }
            }
        }

        // 4. 组装 DTO
        List<QuoteCardDTO> result = new ArrayList<>();
        for (Quote q : quotes) {
            QuoteCardDTO dto = new QuoteCardDTO();
            dto.setQuoteInfo(q);

            User user = userMap.get(q.getUserId());
            dto.setUserNickName(user != null ? user.getNickName() : "未知用户");
            dto.setUserAvatarPath(user != null ? user.getAvatar() : "");

            dto.setPicturePaths(quotePictureMap.getOrDefault(q.getQuoteId(), Collections.emptyList()));
            dto.setLiked(likedQuoteIds.contains(q.getQuoteId()));

            result.add(dto);
        }
        return result;
    }

    /**
     * 聚合接口：获取单条语录的完整详情（含用户信息、全部图片路径、点赞状态）
     */
    public QuoteCardDTO getQuoteCardDetail(Integer quoteId, String currentUserId) {
        Quote quote = quoteRepository.findById(quoteId).orElse(null);
        if (quote == null) return null;

        QuoteCardDTO dto = new QuoteCardDTO();
        dto.setQuoteInfo(quote);

        // 用户信息
        User user = userRepository.findById(quote.getUserId()).orElse(null);
        dto.setUserNickName(user != null ? user.getNickName() : "未知用户");
        dto.setUserAvatarPath(user != null ? user.getAvatar() : "");

        // 图片路径
        List<QuotePicture> pics = getPicturesByQuoteId(quoteId);
        dto.setPicturePaths(pics.stream().map(QuotePicture::getFilePath).collect(Collectors.toList()));

        // 点赞状态
        if (currentUserId != null && !currentUserId.isEmpty()) {
            dto.setLiked(quoteLikeRepository.findByQuoteIdAndUserId(quoteId, currentUserId) != null);
        } else {
            dto.setLiked(false);
        }

        return dto;
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