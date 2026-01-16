package com.mnu.myblog.domain;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class UserDTO {

    private Long id;              // 🔑 DB PK
    private String userId;        // 로그인 ID
    private String userPw;
    private String userName;
    private String phone;
    private String role;          // ROLE_USER / ROLE_ADMIN
    private String profileMessage;

    // 🔥 프로필 이미지 경로 (추가)
    private String profileImage;

    private LocalDateTime createdAt;

    /* ================= 회원 정지 ================= */

    // 정지 여부
    private boolean banned;

    // 정지 시작일 (선택)
    private LocalDateTime bannedAt;

    // 정지 사유 (선택)
    private String banReason;

    /* ================= 권한 체크 ================= */

    // 관리자 여부
    public boolean isAdmin() {
        return "ROLE_ADMIN".equals(this.role);
    }

    // 정지 회원 여부
    public boolean isBanned() {
        return banned;
    }
}