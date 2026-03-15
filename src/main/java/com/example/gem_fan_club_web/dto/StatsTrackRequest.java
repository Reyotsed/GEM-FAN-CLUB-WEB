package com.example.gem_fan_club_web.dto;

import lombok.Data;

@Data
public class StatsTrackRequest {
    /**
     * 事件类型: "pv"(页面访问) 或 "event"(用户行为事件)
     */
    private String type;

    /**
     * 事件标识
     * PV 类型: home, song, quote, quote_detail, picture, shop, ai, info, games, guess_song, lyrics_chain, ticket_rush, quiz, user
     * Event 类型: game_start:guess_song, game_complete:guess_song, game_start:lyrics_chain, game_complete:lyrics_chain, ai_chat, song_play, quote_like, quote_comment, login, register
     */
    private String key;

    /**
     * 用户ID（可选，用于 UV 统计）
     */
    private String userId;
}
