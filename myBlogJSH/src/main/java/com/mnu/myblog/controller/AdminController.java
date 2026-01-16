package com.mnu.myblog.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mnu.myblog.domain.AdminLogType;
import com.mnu.myblog.domain.CommentDTO;
import com.mnu.myblog.domain.PostDTO;
import com.mnu.myblog.domain.UserDTO;
import com.mnu.myblog.service.AdminLogService;
import com.mnu.myblog.service.CommentService;
import com.mnu.myblog.service.NoticeService;
import com.mnu.myblog.service.PostService;
import com.mnu.myblog.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final AdminLogService adminLogService;
    private final NoticeService noticeService;
    private final PostService postService;
    private final CommentService commentService;

    public AdminController(UserService userService,
                           AdminLogService adminLogService,
                           NoticeService noticeService,
                           PostService postService,
                           CommentService commentService) {
        this.userService = userService;
        this.adminLogService = adminLogService;
        this.noticeService = noticeService;
        this.postService = postService;
        this.commentService = commentService;
    }
    

    /* ==================================================
       🔐 管理者チェック
    ================================================== */
    private UserDTO adminCheck(HttpSession session, RedirectAttributes ra) {
        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null || !"ADMIN".equals(loginUser.getRole())) {
            ra.addFlashAttribute("toastMsg", "管理者のみアクセス可能です。");
            ra.addFlashAttribute("toastType", "error");
            return null;
        }
        return loginUser;
    }

    /* ==================================================
       🏠 管理者ホーム
    ================================================== */
    @GetMapping("")
    public String adminHome(HttpSession session,
                            RedirectAttributes ra,
                            Model model) {

        UserDTO admin = adminCheck(session, ra);
        if (admin == null) return "redirect:/";

        model.addAttribute("admin", admin);
        model.addAttribute("postCount", postService.getPostCount(null));
        model.addAttribute("commentCount", commentService.getTotalCount());
        model.addAttribute("userCount", userService.getTotalUserCount());
        model.addAttribute("noticeStats", noticeService.getNoticeStats());

        return "admin/index";
    }

    /* ==================================================
       📝 投稿管理（一覧）
    ================================================== */
    @GetMapping("/posts")
    public String adminPostList(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "status", required = false) String status,
            HttpSession session,
            RedirectAttributes ra,
            Model model) {

        UserDTO admin = adminCheck(session, ra);
        if (admin == null) return "redirect:/";

        int pageSize = 10;
        int offset = page * pageSize;

        List<PostDTO> postList =
                postService.getPostListAdminPagedWithFilter(
                        pageSize, offset, keyword, status
                );

        int totalCount =
                postService.getPostAdminCountWithFilter(keyword, status);

        int totalPages = (int) Math.ceil((double) totalCount / pageSize);

        model.addAttribute("postList", postList);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);

        return "admin/posts";
    }

    /* ==================================================
       📌 投稿固定 ON / OFF
    ================================================== */
    @PostMapping("/posts/pin")
    public String pinPost(@RequestParam("postId") Long postId,
                          HttpSession session,
                          RedirectAttributes ra) {

        UserDTO admin = adminCheck(session, ra);
        if (admin == null) return "redirect:/";

        PostDTO post = postService.getPost(postId);

        if (post.isPinned()) {
            postService.unpin(postId);
            ra.addFlashAttribute("toastMsg", "固定を解除しました。");
        } else {
            postService.pin(postId);
            ra.addFlashAttribute("toastMsg", "投稿を固定しました。");
        }

        ra.addFlashAttribute("toastType", "success");

        adminLogService.writeLog(
                admin.getUserId(),
                AdminLogType.POST_PIN,
                "postId=" + postId
        );

        return "redirect:/admin/posts";
    }

    /* ==================================================
       👁 投稿表示 / 非表示
    ================================================== */
    @PostMapping("/posts/hide")
    public String hidePost(@RequestParam("postId") Long postId,
                           HttpSession session,
                           RedirectAttributes ra) {

        UserDTO admin = adminCheck(session, ra);
        if (admin == null) return "redirect:/";

        PostDTO post = postService.getPost(postId);

        if (post.isVisible()) {
            postService.hide(postId);
            ra.addFlashAttribute("toastMsg", "投稿を非公開にしました。");
        } else {
            postService.show(postId);
            ra.addFlashAttribute("toastMsg", "投稿を公開しました。");
        }

        ra.addFlashAttribute("toastType", "success");

        adminLogService.writeLog(
                admin.getUserId(),
                AdminLogType.POST_HIDE,
                "postId=" + postId
        );

        return "redirect:/admin/posts";
    }

    /* ==================================================
       🗑 投稿削除（SOFT）
    ================================================== */
    @PostMapping("/posts/delete")
    public String deletePost(@RequestParam("postId") Long postId,
                             HttpSession session,
                             RedirectAttributes ra) {

        UserDTO admin = adminCheck(session, ra);
        if (admin == null) return "redirect:/";

        postService.deleteByAdmin(postId);

        adminLogService.writeLog(
                admin.getUserId(),
                AdminLogType.POST_DELETE,
                "postId=" + postId
        );

        ra.addFlashAttribute("toastMsg", "投稿を削除しました。");
        ra.addFlashAttribute("toastType", "warning");

        return "redirect:/admin/posts";
    }

    /* ==================================================
       🧹 削除済み投稿 一括整理
    ================================================== */
    @PostMapping("/posts/cleanup")
    public String cleanupDeletedPosts(HttpSession session,
                                      RedirectAttributes ra) {

        UserDTO admin = adminCheck(session, ra);
        if (admin == null) return "redirect:/";

        postService.cleanupDeletedPosts();

        adminLogService.writeLog(
                admin.getUserId(),
                AdminLogType.POST_DELETE,
                "deleted posts cleanup"
        );

        ra.addFlashAttribute("toastMsg", "削除済み投稿を整理しました。");
        ra.addFlashAttribute("toastType", "success");

        return "redirect:/admin/posts?status=DELETED";
    }

    /* ==================================================
       ✏ 投稿編集（移動）
    ================================================== */
    @GetMapping("/posts/edit")
    public String editPost(@RequestParam("postId") Long postId,
                           HttpSession session,
                           RedirectAttributes ra) {

        UserDTO admin = adminCheck(session, ra);
        if (admin == null) return "redirect:/";

        return "redirect:/post/edit/" + postId;
    }

    /* ==================================================
       💬 コメント管理
    ================================================== */
    @GetMapping("/comments")
    public String adminComments(
            @RequestParam(name = "page", defaultValue = "0") int page,
            HttpSession session,
            RedirectAttributes ra,
            Model model) {

        UserDTO admin = adminCheck(session, ra);
        if (admin == null) return "redirect:/";

        int pageSize = 10;
        int offset = page * pageSize;

        List<CommentDTO> commentList =
                commentService.getAllCommentsPaged(pageSize, offset);

        int totalCount = commentService.getTotalCountAdmin();
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);

        model.addAttribute("commentList", commentList);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);

        return "admin/comments";
    }

    /* ==================================================
       👤 会員管理
    ================================================== */
    @GetMapping("/users")
    public String adminUsers(HttpSession session,
                             RedirectAttributes ra,
                             Model model) {

        UserDTO admin = adminCheck(session, ra);
        if (admin == null) return "redirect:/";

        model.addAttribute("userList", userService.getAllUsers());
        return "admin/user_list";
    }
    
}
