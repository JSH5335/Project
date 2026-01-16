package com.mnu.myblog.domain;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class PostDTO {

    private Long postId;

    private String title;
    private String content;

    // 🔐 작성자
    private String writerId;
    private String writerName;

    // 📊 카운트
    private int viewCount;
    private int likeCount;
    private int commentCount; // 댓글 개수
    private boolean hasReply; // 대댓글 존재 여부

    // 👁 노출 여부 (Soft Delete 시 false)
    private boolean isVisible;

    // 🗑 Soft Delete 여부
    private boolean isDeleted;

    // 📌 고정글
    private boolean isPinned;

    // ⏰ 날짜
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 👑 작성자 권한
    private String writerRole;

    /* ================= 📎 파일 ================= */

    // 서버에 저장된 파일 경로
    private String filePath;

    // 원본 파일명 (다운로드용)
    private String originalFileName;
}