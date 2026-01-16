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

import com.mnu.myblog.domain.AdminLogType;
import com.mnu.myblog.domain.CommentDTO;
import com.mnu.myblog.domain.UserDTO;
import com.mnu.myblog.service.AdminLogService;
import com.mnu.myblog.service.CommentService;

@Controller
@RequestMapping("/comment")
public class CommentController {

    private final CommentService commentService;
    private final AdminLogService adminLogService;

    public CommentController(CommentService commentService,
                             AdminLogService adminLogService) {
        this.commentService = commentService;
        this.adminLogService = adminLogService;
    }

    /* ================= コメント作成（通常 / 返信） ================= */
    @PostMapping("/write")
    public String write(@RequestParam("postId") Long postId,
                        @RequestParam("content") String content,
                        @RequestParam(value = "parentId", required = false) Long parentId,
                        HttpSession session,
                        RedirectAttributes ra) {

        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");

        if (loginUser == null) {
            ra.addFlashAttribute("toastMsg", "ログインが必要です。");
            ra.addFlashAttribute("toastType", "error");
            return "redirect:/login";
        }

        if (content == null || content.trim().isEmpty()) {
            ra.addFlashAttribute("toastMsg", "コメントを入力してください。");
            ra.addFlashAttribute("toastType", "error");
            return "redirect:/post/view/" + postId;
        }

        CommentDTO comment = new CommentDTO();
        comment.setPostId(postId);
        comment.setUserId(loginUser.getUserId());
        comment.setUserName(loginUser.getUserName());
        comment.setContent(content.trim());

        // ✅ parentId 처리 (대댓글)
        if (parentId != null && parentId > 0) {
            comment.setParentId(parentId);
        } else {
            comment.setParentId(null);
        }

        // ✅ void 메서드 호출
        commentService.write(comment);

        ra.addFlashAttribute(
                "toastMsg",
                comment.getParentId() == null
                        ? "コメントを登録しました。"
                        : "返信を登録しました。"
        );
        ra.addFlashAttribute("toastType", "success");

        return "redirect:/post/view/" + postId;
    }

    /* ================= コメント削除（作成者・SOFT） ================= */
    @PostMapping("/delete")
    public String delete(@RequestParam("commentId") Long commentId,
                         @RequestParam(value = "postId", required = false) Long postId,
                         HttpSession session,
                         RedirectAttributes ra) {

        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");

        if (loginUser == null) {
            ra.addFlashAttribute("toastMsg", "ログインが必要です。");
            ra.addFlashAttribute("toastType", "error");
            return "redirect:/login";
        }

        boolean success =
                commentService.deleteByUser(commentId, loginUser.getUserId());

        if (!success) {
            ra.addFlashAttribute("toastMsg", "削除権限がありません。");
            ra.addFlashAttribute("toastType", "error");
        } else {
            adminLogService.writeLog(
                    loginUser.getUserId(),
                    AdminLogType.COMMENT_DELETE,
                    "comment#" + commentId
            );

            ra.addFlashAttribute("toastMsg", "コメントを削除しました。");
            ra.addFlashAttribute("toastType", "success");
        }

        if (postId != null) {
            return "redirect:/post/view/" + postId;
        }
        return "redirect:/archive";
    }

    /* ================= コメント削除（管理者・HARD） ================= */
    @PostMapping("/admin/delete")
    public String deleteByAdmin(@RequestParam("commentId") Long commentId,
                                @RequestParam(value = "parentId", required = false) Long parentId,
                                @RequestParam(value = "postId", required = false) Long postId,
                                HttpSession session,
                                RedirectAttributes ra) {

        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");

        if (loginUser == null || !loginUser.isAdmin()) {
            ra.addFlashAttribute("toastMsg", "管理者権限が必要です。");
            ra.addFlashAttribute("toastType", "error");
            return "redirect:/archive";
        }

        commentService.deleteByAdminHard(commentId, parentId);

        adminLogService.writeLog(
                loginUser.getUserId(),
                AdminLogType.COMMENT_DELETE,
                "comment#" + commentId + " (hard)"
        );

        ra.addFlashAttribute("toastMsg", "コメントを完全に削除しました。");
        ra.addFlashAttribute("toastType", "success");

        if (postId != null) {
            return "redirect:/post/view/" + postId;
        }
        return "redirect:/archive";
    }

    /* ================= コメント通報 ================= */
    @PostMapping("/report")
    public String report(@RequestParam("commentId") Long commentId,
                         @RequestParam(value = "postId", required = false) Long postId,
                         @RequestParam(value = "reason", required = false) String reason,
                         HttpSession session,
                         RedirectAttributes ra) {

        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");

        if (loginUser == null) {
            ra.addFlashAttribute("toastMsg", "ログインが必要です。");
            ra.addFlashAttribute("toastType", "error");
            return "redirect:/login";
        }

        if (reason == null || reason.trim().isEmpty()) {
            reason = "ユーザー通報";
        }

        boolean success = commentService.report(
                commentId,
                loginUser.getUserId(),
                reason.trim()
        );

        if (!success) {
            ra.addFlashAttribute("toastMsg", "このコメントは通報できません。");
            ra.addFlashAttribute("toastType", "error");
        } else {
            adminLogService.writeLog(
                    loginUser.getUserId(),
                    AdminLogType.COMMENT_REPORT_IGNORE,
                    "comment#" + commentId
            );

            ra.addFlashAttribute("toastMsg", "コメントを通報しました。");
            ra.addFlashAttribute("toastType", "success");
        }

        if (postId != null) {
            return "redirect:/post/view/" + postId;
        }
        return "redirect:/archive";
    }

    /* ================= 管理者：通報コメント一覧 ================= */
    @GetMapping("/admin/reported")
    public String reportedComments(HttpSession session, Model model) {

        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");

        if (loginUser == null || !loginUser.isAdmin()) {
            return "redirect:/archive";
        }

        List<CommentDTO> reportedComments =
                commentService.getReportedComments();

        model.addAttribute("reportedComments", reportedComments);
        return "admin/comments_reported";
    }

    /* ================= 管理者：通報コメント SOFT 削除 ================= */
    @PostMapping("/admin/reported/soft-delete")
    public String softDeleteReported(@RequestParam("commentId") Long commentId,
                                     HttpSession session,
                                     RedirectAttributes ra) {

        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");

        if (loginUser == null || !loginUser.isAdmin()) {
            ra.addFlashAttribute("toastMsg", "管理者権限が必要です。");
            ra.addFlashAttribute("toastType", "error");
            return "redirect:/archive";
        }

        commentService.softDeleteByAdmin(commentId);

        adminLogService.writeLog(
                loginUser.getUserId(),
                AdminLogType.COMMENT_REPORT_SOFT_DELETE,
                "comment#" + commentId
        );

        ra.addFlashAttribute("toastMsg", "コメントを非表示にしました。");
        ra.addFlashAttribute("toastType", "warning");

        return "redirect:/comment/admin/reported";
    }

    /* ================= 管理者：通報コメント HARD 削除 ================= */
    @PostMapping("/admin/reported/hard-delete")
    public String hardDeleteReported(@RequestParam("commentId") Long commentId,
                                     @RequestParam(value = "parentId", required = false) Long parentId,
                                     HttpSession session,
                                     RedirectAttributes ra) {

        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");

        if (loginUser == null || !loginUser.isAdmin()) {
            ra.addFlashAttribute("toastMsg", "管理者権限が必要です。");
            ra.addFlashAttribute("toastType", "error");
            return "redirect:/archive";
        }

        commentService.deleteByAdminHard(commentId, parentId);

        adminLogService.writeLog(
                loginUser.getUserId(),
                AdminLogType.COMMENT_REPORT_HARD_DELETE,
                "comment#" + commentId
        );

        ra.addFlashAttribute("toastMsg", "コメントを完全に削除しました。");
        ra.addFlashAttribute("toastType", "error");

        return "redirect:/comment/admin/reported";
    }
    
    /* ================= コメント削除（管理者・SOFT） ================= */
    @PostMapping("/admin/soft-delete")
    public String softDeleteByAdmin(@RequestParam("commentId") Long commentId,
                                    HttpSession session,
                                    RedirectAttributes ra) {

        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");

        if (loginUser == null || !loginUser.isAdmin()) {
            ra.addFlashAttribute("toastMsg", "管理者権限が必要です。");
            ra.addFlashAttribute("toastType", "error");
            return "redirect:/archive";
        }

        // ✅ 관리자 SOFT DELETE
        commentService.softDeleteByAdmin(commentId);

        adminLogService.writeLog(
                loginUser.getUserId(),
                AdminLogType.COMMENT_DELETE,
                "comment#" + commentId + " (soft)"
        );

        ra.addFlashAttribute("toastMsg", "管理者により削除されました。");
        ra.addFlashAttribute("toastType", "warning");

        // ✅ 관리자 댓글 관리 페이지로 복귀
        return "redirect:/admin/comments";
    }
}