package com.mnu.myblog.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mnu.myblog.domain.UserDTO;

@Mapper
public interface UserMapper {

    /* ================= 회원 ================= */

    // 아이디 중복 체크
    int countByUserId(String userId);

    // 회원가입
    int insertUser(UserDTO user);

    // 로그인
    UserDTO findByUserId(String userId);

    // 개인 메시지 수정
    int updateProfileMessage(
            @Param("userId") String userId,
            @Param("profileMessage") String profileMessage
    );

    // 🔥 프로필 이미지 수정 (추가)
    int updateProfileImage(
            @Param("userId") String userId,
            @Param("profileImage") String profileImage
    );

    /* ================= 관리자 ================= */

    // 전체 회원 조회
    List<UserDTO> findAllUsers();

    // 전체 회원 수 (관리자 대시보드)
    int selectTotalUserCount();

    // 회원 권한 변경
    int updateRole(
            @Param("userId") String userId,
            @Param("role") String role
    );

    /* ================= 🔒 회원 정지 ================= */

    // 회원 정지
    int banUser(
            @Param("userId") String userId,
            @Param("banReason") String banReason
    );

    // 회원 정지 해제
    int unbanUser(
            @Param("userId") String userId
    );

    int checkPassword(
            @Param("userId") String userId,
            @Param("userPw") String userPw
    );

    void withdraw(@Param("userId") String userId);
}
