package com.mnu.myblog.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class CommentDTO {

    private Long commentId;
    private Long postId;

    private String userId;
    private String userName;

    private String content;
    private LocalDateTime createdAt;

    private Long parentId;
    private int depth;

    private List<CommentDTO> replyList = new ArrayList<>();

    // 🔥 DB 매핑용 (is_deleted)
    private boolean deleted;
    private String deletedBy;
    
    // 🔥 원본 댓글 내용 (관리자/유저 복구용)
    private String originalContent;

}