package com.mnu.myblog.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.mnu.myblog.service.NoticeService;
import com.mnu.myblog.service.PostService;

@Controller
public class HomeController {

    private final NoticeService noticeService;
    private final PostService postService;

    public HomeController(NoticeService noticeService,
                          PostService postService) {
        this.noticeService = noticeService;
        this.postService = postService;
    }

    /* ================= 메인 페이지 ================= */

    @GetMapping("/")
    public String home(Model model) {

        // 📢 최신 공지 3개
        model.addAttribute("noticeList",
                noticeService.getLatestNotices());

        // 🔥 인기글 TOP 3
        model.addAttribute("popularPostList",
                postService.getPopularTop3());

        return "index";
    }
}