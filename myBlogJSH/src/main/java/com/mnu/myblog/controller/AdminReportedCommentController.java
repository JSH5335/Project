package com.mnu.myblog.controller;

import java.util.List;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mnu.myblog.domain.CommentDTO;
import com.mnu.myblog.domain.UserDTO;
import com.mnu.myblog.domain.AdminLogType;
import com.mnu.myblog.service.CommentService;
import com.mnu.myblog.service.AdminLogService;

@Controller
@RequestMapping("/admin/reported-comments")
public class AdminReportedCommentController {

    private final CommentService commentService;
    private final AdminLogService adminLogService;

    public AdminReportedCommentController(CommentService commentService,
                                          AdminLogService adminLogService) {
        this.commentService = commentService;
        this.adminLogService = adminLogService;
    }

    /* ================= 신고 댓글 목록 (페이징) ================= */
    @GetMapping
    public String reportedComments(
            @RequestParam(value = "page", defaultValue = "0") int page,
            HttpSession session,
            Model model,
            RedirectAttributes ra) {

        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");

        if (loginUser == null || !"ADMIN".equals(loginUser.getRole())) {
            ra.addFlashAttribute("toastMsg", "管理者権限が必要です。");
            ra.addFlashAttribute("toastType", "error");
            return "redirect:/";
        }

        int pageSize = 10;
        int offset = page * pageSize;

        List<CommentDTO> reportedComments =
                commentService.getReportedCommentsPaged(pageSize, offset);

        int totalCount = commentService.getReportedCommentsCount();
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);

        model.addAttribute("reportedComments", reportedComments);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);

        return "admin/comments_reported";
    }

    /* ================= 신고 댓글 SOFT DELETE (비공개 처리) ================= */
    @PostMapping("/soft-delete")
    public String softDeleteReportedComment(
            @RequestParam("commentId") Long commentId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            HttpSession session,
            RedirectAttributes ra) {

        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");

        if (loginUser == null || !"ADMIN".equals(loginUser.getRole())) {
            ra.addFlashAttribute("toastMsg", "管理者権限が必要です。");
            ra.addFlashAttribute("toastType", "error");
            return "redirect:/";
        }

        commentService.deleteByAdminSoft(commentId);
        commentService.ignoreReport(commentId);

        adminLogService.writeLog(
                loginUser.getUserId(),
                AdminLogType.COMMENT_REPORT_SOFT_DELETE,
                "commentId=" + commentId
        );

        ra.addFlashAttribute("toastMsg", "コメントを非表示にしました。");
        ra.addFlashAttribute("toastType", "success");

        return "redirect:/admin/reported-comments?page=" + page;
    }

    /* ================= 신고 댓글 HARD DELETE ================= */
    @PostMapping("/hard-delete")
    public String hardDeleteReportedComment(
            @RequestParam("commentId") Long commentId,
            @RequestParam(value = "parentId", required = false) Long parentId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            HttpSession session,
            RedirectAttributes ra) {

        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");

        if (loginUser == null || !"ADMIN".equals(loginUser.getRole())) {
            ra.addFlashAttribute("toastMsg", "管理者権限が必要です。");
            ra.addFlashAttribute("toastType", "error");
            return "redirect:/";
        }

        commentService.deleteReportedComment(commentId, parentId);

        adminLogService.writeLog(
                loginUser.getUserId(),
                AdminLogType.COMMENT_REPORT_HARD_DELETE,
                "commentId=" + commentId
        );

        ra.addFlashAttribute("toastMsg", "通報コメントを完全に削除しました。");
        ra.addFlashAttribute("toastType", "success");

        return "redirect:/admin/reported-comments?page=" + page;
    }

    /* ================= 신고 무시 ================= */
    @PostMapping("/ignore")
    public String ignoreReport(
            @RequestParam("commentId") Long commentId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            HttpSession session,
            RedirectAttributes ra) {

        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");

        if (loginUser == null || !"ADMIN".equals(loginUser.getRole())) {
            ra.addFlashAttribute("toastMsg", "管理者権限が必要です。");
            ra.addFlashAttribute("toastType", "error");
            return "redirect:/";
        }

        commentService.ignoreReport(commentId);

        adminLogService.writeLog(
                loginUser.getUserId(),
                AdminLogType.COMMENT_REPORT_IGNORE,
                "commentId=" + commentId
        );

        ra.addFlashAttribute("toastMsg", "通報を無視しました。");
        ra.addFlashAttribute("toastType", "success");

        return "redirect:/admin/reported-comments?page=" + page;
    }
}
