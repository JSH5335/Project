package com.mnu.myblog.domain;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class NoticeDTO {

    /* ===== PK ===== */
    private Long noticeId;

    /* ===== 내용 ===== */
    private String title;
    private String content;
    private String imagePath;

    /* ===== 상태 ===== */
    private int visible;   // 1: 보임, 0: 숨김
    private int pinned;    // 1: 고정, 0: 해제

    /* ===== 카운트 ===== */
    private int views;      
    private int checkCount;

    /* ===== 날짜 ===== */
    private LocalDateTime createdAt;
}