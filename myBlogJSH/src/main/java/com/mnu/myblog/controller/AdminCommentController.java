package com.mnu.myblog.controller;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mnu.myblog.domain.AdminLogType;
import com.mnu.myblog.domain.UserDTO;
import com.mnu.myblog.service.AdminLogService;
import com.mnu.myblog.service.CommentService;

@Controller
@RequestMapping("/admin/comments")
public class AdminCommentController {

    private final CommentService commentService;
    private final AdminLogService adminLogService;

    public AdminCommentController(CommentService commentService,
                                  AdminLogService adminLogService) {
        this.commentService = commentService;
        this.adminLogService = adminLogService;
    }

    /* ================= SOFT DELETE ================= */
    @PostMapping("/delete")
    public String softDelete(@RequestParam("commentId") Long commentId,
                             @RequestParam(value = "postId", required = false) Long postId,
                             @RequestParam(value = "page", required = false) Integer page,
                             HttpSession session,
                             RedirectAttributes ra) {

        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");

        // 🔥 핵심 수정
        if (loginUser == null || !"ADMIN".equals(loginUser.getRole())) {
            ra.addFlashAttribute("toastMsg", "権限がありません。");
            ra.addFlashAttribute("toastType", "error");
            return postId != null
                    ? "redirect:/post/view/" + postId
                    : "redirect:/admin/comments";
        }

        commentService.deleteByAdminSoft(commentId);

        adminLogService.writeLog(
                loginUser.getUserId(),
                AdminLogType.COMMENT_DELETE,
                "comment#" + commentId + " (soft)"
        );

        ra.addFlashAttribute("toastMsg", "管理者により削除されました。");
        ra.addFlashAttribute("toastType", "warning");

        if (postId != null) {
            return "redirect:/post/view/" + postId;
        }

        return page != null
                ? "redirect:/admin/comments?page=" + page
                : "redirect:/admin/comments";
    }

    /* ================= HARD DELETE ================= */
    @PostMapping("/hard-delete")
    public String hardDelete(@RequestParam("commentId") Long commentId,
                             @RequestParam(value = "parentId", required = false) Long parentId,
                             @RequestParam(value = "postId", required = false) Long postId,
                             @RequestParam(value = "page", required = false) Integer page,
                             HttpSession session,
                             RedirectAttributes ra) {

        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");

        // 🔥 핵심 수정
        if (loginUser == null || !"ADMIN".equals(loginUser.getRole())) {
            ra.addFlashAttribute("toastMsg", "権限がありません。");
            ra.addFlashAttribute("toastType", "error");
            return postId != null
                    ? "redirect:/post/view/" + postId
                    : "redirect:/admin/comments";
        }

        commentService.deleteByAdminHard(commentId, parentId);

        adminLogService.writeLog(
                loginUser.getUserId(),
                AdminLogType.COMMENT_DELETE,
                "comment#" + commentId + " (hard)"
        );

        ra.addFlashAttribute("toastMsg", "コメントを完全に削除しました。");
        ra.addFlashAttribute("toastType", "warning");

        if (postId != null) {
            return "redirect:/post/view/" + postId;
        }

        return page != null
                ? "redirect:/admin/comments?page=" + page
                : "redirect:/admin/comments";
    }

    /* ================= RESTORE ================= */
    @PostMapping("/restore")
    public String restore(@RequestParam("commentId") Long commentId,
                          HttpSession session,
                          RedirectAttributes ra) {

        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");

        // 🔥 핵심 수정
        if (loginUser == null || !"ADMIN".equals(loginUser.getRole())) {
            ra.addFlashAttribute("toastMsg", "管理者権限が必要です。");
            ra.addFlashAttribute("toastType", "error");
            return "redirect:/admin/comments";
        }

        commentService.restoreByAdmin(commentId);

        adminLogService.writeLog(
                loginUser.getUserId(),
                AdminLogType.COMMENT_RESTORE,
                "comment#" + commentId
        );

        ra.addFlashAttribute("toastMsg", "コメントを復元しました。");
        ra.addFlashAttribute("toastType", "success");

        return "redirect:/admin/comments";
    }
}