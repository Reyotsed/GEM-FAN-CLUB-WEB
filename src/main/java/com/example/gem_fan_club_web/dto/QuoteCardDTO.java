package com.example.gem_fan_club_web.dto;

import com.example.gem_fan_club_web.model.quote.Quote;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 语录卡片聚合 DTO — 一次性返回前端列表渲染所需的所有数据，
 * 避免前端为每条语录发起 N+1 次请求。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuoteCardDTO {
    /** 语录基本信息 */
    private Quote quoteInfo;
    /** 发布者昵称 */
    private String userNickName;
    /** 发布者头像文件路径（前端再用此路径请求图片） */
    private String userAvatarPath;
    /** 语录关联的图片文件路径列表 */
    private List<String> picturePaths;
    /** 当前用户是否已点赞（未登录时为 false） */
    private boolean liked;
}
