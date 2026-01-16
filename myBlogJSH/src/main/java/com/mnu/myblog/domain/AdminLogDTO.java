package com.mnu.myblog.domain;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AdminLogDTO {

    private Long id;              // 로그 PK
    private String adminId;       // 관리자 아이디 (user_id)
    private String action;        // 행동
    private String target;        // 대상
    private LocalDateTime createdAt;
}