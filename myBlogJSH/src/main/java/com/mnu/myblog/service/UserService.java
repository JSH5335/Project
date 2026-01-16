package com.mnu.myblog.service;

import java.util.List;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.mnu.myblog.domain.UserDTO;
import com.mnu.myblog.mapper.UserMapper;

@Service
public class UserService {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /* ================= 회원 ================= */

    // 아이디 중복 체크
    public boolean isDuplicatedId(String userId) {
        return userMapper.countByUserId(userId) > 0;
    }

 // 회원가입
    public void register(UserDTO user) {
        user.setUserPw(encoder.encode(user.getUserPw()));

        // ✅ 기본 상태 메시지 (일본어)
        if (user.getProfileMessage() == null || user.getProfileMessage().isBlank()) {
            user.setProfileMessage("ご自由にご記入ください。✏️");
        }

        userMapper.insertUser(user);
    }

    // 로그인
    public UserDTO login(String userId, String rawPassword) {
        UserDTO user = userMapper.findByUserId(userId);

        if (user == null) {
            return null;
        }

        if (!encoder.matches(rawPassword, user.getUserPw())) {
            return null;
        }

        // 정지 회원 로그인 차단
        if (user.isBanned()) {
            return null;
        }

        return user;
    }

    // 🔥 userId로 단일 회원 조회 (탈퇴용)
    public UserDTO findByUserId(String userId) {
        return userMapper.findByUserId(userId);
    }

    // 개인 메시지 수정
    public void updateProfileMessage(String userId, String profileMessage) {
        userMapper.updateProfileMessage(userId, profileMessage);
    }
    
    // 🔥 프로필 이미지 수정
    public void updateProfileImage(String userId, String profileImage) {
        userMapper.updateProfileImage(userId, profileImage);
    }

    /* ================= 관리자 ================= */

    // 전체 회원 조회
    public List<UserDTO> getAllUsers() {
        return userMapper.findAllUsers();
    }

    // 전체 회원 수
    public int getTotalUserCount() {
        return userMapper.selectTotalUserCount();
    }

    // 권한 변경
    public void changeUserRole(String userId, String role) {
        userMapper.updateRole(userId, role);
    }

    /* ================= 🔒 회원 정지 / 탈퇴 ================= */

    // 회원 정지
    public void banUser(String userId, String reason) {
        userMapper.banUser(userId, reason);
    }

    // 회원 정지 해제
    public void unbanUser(String userId) {
        userMapper.unbanUser(userId);
    }

    // 회원 탈퇴 (소프트 탈퇴: banned 처리)
    public void withdraw(String userId) {
        userMapper.withdraw(userId);
    }
    
}
