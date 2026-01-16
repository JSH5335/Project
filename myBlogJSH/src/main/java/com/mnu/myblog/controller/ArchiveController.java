package com.mnu.myblog.controller;

import java.util.List;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mnu.myblog.domain.PostDTO;
import com.mnu.myblog.domain.UserDTO;
import com.mnu.myblog.service.PostService;

@Controller
public class ArchiveController {

    private final PostService postService;

    public ArchiveController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/archive")
    public String archive(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "keyword", required = false) String keyword,
            HttpSession session,
            Model model,
            RedirectAttributes ra) {

        /* ================= 로그인 체크 ================= */
        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) {
            ra.addFlashAttribute("toastMsg", "로그인이 필요합니다.");
            ra.addFlashAttribute("toastType", "error");
            return "redirect:/login";
        }

        /* ================= 페이징 ================= */
        int size = 10;
        int offset = (page - 1) * size;

        int totalCount = postService.getPostCount(keyword);
        int totalPage = (int) Math.ceil((double) totalCount / size);

        if (page < 1) page = 1;
        if (page > totalPage && totalPage > 0) page = totalPage;

        List<PostDTO> postList =
                postService.getPostListPaged(size, offset, keyword);

        /* ================= View 전달 ================= */
        model.addAttribute("postList", postList);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPage", totalPage);
        model.addAttribute("keyword", keyword);

        return "archive/list";
    }
}