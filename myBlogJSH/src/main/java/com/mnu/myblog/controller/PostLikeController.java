package com.mnu.myblog.controller;

import com.mnu.myblog.domain.UserDTO;
import com.mnu.myblog.service.PostLikeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PostLikeController {

    private final PostLikeService postLikeService;

    public PostLikeController(PostLikeService postLikeService) {
        this.postLikeService = postLikeService;
    }

    /**
     * ❤️ 게시글 좋아요
     * - 로그인 필수
     * - 중복 좋아요 완전 차단
     * - DB 에러 발생 없음
     */
    @PostMapping("/post/like")
    public String likePost(@RequestParam("postId") Long postId,
                           HttpSession session,
                           RedirectAttributes ra) {

        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");

        // 🔐 로그인 체크
        if (loginUser == null) {
            ra.addFlashAttribute("toastMsg", "ログインが必要です。");
            ra.addFlashAttribute("toastType", "error");
            return "redirect:/post/view/" + postId;
        }

        boolean success = postLikeService.likeOncePerDay(
                postId,
                loginUser.getUserId()
        );

        // ❌ 이미 좋아요 상태
        if (!success) {
            ra.addFlashAttribute("toastMsg", "すでに「いいね」しています。");
            ra.addFlashAttribute("toastType", "info");
        }
        // ❤️ 좋아요 성공
        else {
            ra.addFlashAttribute("toastMsg", "記事に「いいね」しました ❤️");
            ra.addFlashAttribute("toastType", "success");
        }

        return "redirect:/post/view/" + postId;
    }
}