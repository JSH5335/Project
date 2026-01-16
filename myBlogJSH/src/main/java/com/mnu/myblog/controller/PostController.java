package com.mnu.myblog.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mnu.myblog.domain.AdminLogType;
import com.mnu.myblog.domain.CommentDTO;
import com.mnu.myblog.domain.PostDTO;
import com.mnu.myblog.domain.UserDTO;
import com.mnu.myblog.service.AdminLogService;
import com.mnu.myblog.service.CommentService;
import com.mnu.myblog.service.PostLikeService;
import com.mnu.myblog.service.PostService;

import org.springframework.http.ResponseEntity;

@Controller
@RequestMapping("/post")
public class PostController {

    private final PostService postService;
    private final PostLikeService postLikeService;
    private final CommentService commentService;
    private final AdminLogService adminLogService;

    private static final String UPLOAD_DIR = "C:/myblog/uploads/post/";

    public PostController(PostService postService,
                          PostLikeService postLikeService,
                          CommentService commentService,
                          AdminLogService adminLogService) {
        this.postService = postService;
        this.postLikeService = postLikeService;
        this.commentService = commentService;
        this.adminLogService = adminLogService;
    }

    /* ================= 記事作成 ================= */
    @GetMapping("/write")
    public String writeForm(HttpSession session, RedirectAttributes ra) {
        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) {
            ra.addFlashAttribute("toastMsg", "ログインが必要です。");
            ra.addFlashAttribute("toastType", "error");
            return "redirect:/login";
        }
        return "post/write";
    }

    @PostMapping("/write")
    public String write(PostDTO post,
                        @RequestParam(value = "uploadFile", required = false) MultipartFile uploadFile,
                        HttpSession session,
                        RedirectAttributes ra) {

        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) {
            ra.addFlashAttribute("toastMsg", "ログインが必要です。");
            ra.addFlashAttribute("toastType", "error");
            return "redirect:/login";
        }

        post.setWriterId(loginUser.getUserId());
        post.setWriterName(loginUser.getUserName());

        if (uploadFile != null && !uploadFile.isEmpty()) {
            try {
                File dir = new File(UPLOAD_DIR);
                if (!dir.exists()) dir.mkdirs();

                String originalName = uploadFile.getOriginalFilename();
                String savedName = UUID.randomUUID() + "_" + originalName;

                File saveFile = new File(dir, savedName);
                uploadFile.transferTo(saveFile);

                post.setFilePath("/upload/post/" + savedName);
                post.setOriginalFileName(originalName);

            } catch (IOException e) {
                ra.addFlashAttribute("toastMsg", "ファイルアップロードに失敗しました。");
                ra.addFlashAttribute("toastType", "error");
                return "redirect:/post/write";
            }
        }

        postService.write(post);

        adminLogService.writeLog(
                loginUser.getUserId(),
                AdminLogType.POST_SHOW,
                "post#" + post.getPostId()
        );

        ra.addFlashAttribute("toastMsg", "記事を登録しました。");
        ra.addFlashAttribute("toastType", "success");
        return "redirect:/archive";
    }

    /* ================= 記事閲覧 ================= */
    @GetMapping("/view/{postId}")
    public String view(@PathVariable("postId") Long postId,
                       HttpSession session,
                       Model model,
                       RedirectAttributes ra) {

        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) {
            ra.addFlashAttribute("toastMsg", "ログインが必要です。");
            ra.addFlashAttribute("toastType", "error");
            return "redirect:/login";
        }

        PostDTO post = postService.getPost(postId);

        if (post == null || post.isDeleted()) {
            ra.addFlashAttribute("toastMsg", "削除された記事です。");
            ra.addFlashAttribute("toastType", "error");
            return "redirect:/archive";
        }

        if (!post.isVisible() && !loginUser.isAdmin()) {
            ra.addFlashAttribute("toastMsg", "非表示の記事です。");
            ra.addFlashAttribute("toastType", "error");
            return "redirect:/archive";
        }

        String viewKey = "viewedPost_" + postId;
        if (session.getAttribute(viewKey) == null) {
            postService.increaseViewCount(postId);
            session.setAttribute(viewKey, true);
        }

        // ✅ 여기만 수정
        List<CommentDTO> commentList;
        if (loginUser.isAdmin()) {
            commentList = commentService.getCommentListWithDeleted(postId);
        } else {
            commentList = commentService.getCommentList(postId);
        }

        boolean liked = postLikeService.existsLike(postId, loginUser.getUserId());

        model.addAttribute("post", post);
        model.addAttribute("commentList", commentList);
        long commentCount = commentList.stream()
                .filter(c -> !c.isDeleted())
                .mapToLong(c ->
                        1 + (c.getReplyList() == null
                                ? 0
                                : c.getReplyList().stream()
                                        .filter(r -> !r.isDeleted())
                                        .count())
                )
                .sum();

        model.addAttribute("commentCount", commentCount);
        model.addAttribute("liked", liked);

        return "post/view";
    }

    /* ================= 記事修正（GET） ================= */
    @GetMapping("/edit/{postId}")
    public String editForm(@PathVariable("postId") Long postId,
                           HttpSession session,
                           Model model,
                           RedirectAttributes ra) {

        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        PostDTO post = postService.getPost(postId);

        if (loginUser == null ||
            post == null ||
            post.isDeleted() ||
            (!loginUser.isAdmin() && !loginUser.getUserId().equals(post.getWriterId()))) {

            ra.addFlashAttribute("toastMsg", "編集権限がありません。");
            ra.addFlashAttribute("toastType", "error");
            return "redirect:/archive";
        }

        model.addAttribute("post", post);
        return "post/edit";
    }

    /* ================= 記事修正（POST） ================= */
    @PostMapping("/edit")
    public String edit(PostDTO post,
                       @RequestParam(value = "uploadFile", required = false) MultipartFile uploadFile,
                       @RequestParam(value = "deleteFile", required = false) Boolean deleteFile,
                       HttpSession session,
                       RedirectAttributes ra) {

        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        PostDTO origin = postService.getPost(post.getPostId());

        if (loginUser == null ||
            origin == null ||
            origin.isDeleted() ||
            (!loginUser.isAdmin() && !loginUser.getUserId().equals(origin.getWriterId()))) {

            ra.addFlashAttribute("toastMsg", "編集権限がありません。");
            ra.addFlashAttribute("toastType", "error");
            return "redirect:/archive";
        }

        try {
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) dir.mkdirs();

            if (Boolean.TRUE.equals(deleteFile)) {
                post.setFilePath(null);
                post.setOriginalFileName(null);
            }

            if (uploadFile != null && !uploadFile.isEmpty()) {
                String originalName = uploadFile.getOriginalFilename();
                String savedName = UUID.randomUUID() + "_" + originalName;
                File saveFile = new File(dir, savedName);
                uploadFile.transferTo(saveFile);

                post.setFilePath("/upload/post/" + savedName);
                post.setOriginalFileName(originalName);
            } else if (!Boolean.TRUE.equals(deleteFile)) {
                post.setFilePath(origin.getFilePath());
                post.setOriginalFileName(origin.getOriginalFileName());
            }

        } catch (IOException e) {
            ra.addFlashAttribute("toastMsg", "ファイル処理中にエラーが発生しました。");
            ra.addFlashAttribute("toastType", "error");
            return "redirect:/archive";
        }

        postService.updatePostWithFile(post);

        adminLogService.writeLog(
                loginUser.getUserId(),
                AdminLogType.POST_SHOW,
                "post#" + post.getPostId()
        );

        ra.addFlashAttribute("toastMsg", "記事を更新しました。");
        ra.addFlashAttribute("toastType", "success");
        return "redirect:/post/view/" + post.getPostId();
    }

    /* ================= 記事非表示（ユーザー） ================= */
    @PostMapping("/delete")
    public String deleteByUser(@RequestParam("postId") Long postId,
                               HttpSession session,
                               RedirectAttributes ra) {

        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        PostDTO post = postService.getPost(postId);

        if (loginUser == null ||
            post == null ||
            post.isDeleted() ||
            !loginUser.getUserId().equals(post.getWriterId())) {

            ra.addFlashAttribute("toastMsg", "削除権限がありません。");
            ra.addFlashAttribute("toastType", "error");
            return "redirect:/archive";
        }

        postService.hide(postId);

        adminLogService.writeLog(
                loginUser.getUserId(),
                AdminLogType.POST_HIDE,
                "post#" + postId
        );

        ra.addFlashAttribute("toastMsg", "記事を非表示にしました。");
        ra.addFlashAttribute("toastType", "warning");
        return "redirect:/archive";
    }

    /* ================= 📎 添付ファイル ダウンロード ================= */
    @GetMapping("/download/{postId}")
    public ResponseEntity<org.springframework.core.io.Resource> downloadFile(
            @PathVariable("postId") Long postId,
            HttpSession session
    ) throws IOException {

        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) {
            return ResponseEntity.status(403).build();
        }

        PostDTO post = postService.getPost(postId);

        if (post == null || post.getFilePath() == null) {
            return ResponseEntity.notFound().build();
        }

        File file = new File(UPLOAD_DIR, new File(post.getFilePath()).getName());

        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        org.springframework.core.io.Resource resource =
                new org.springframework.core.io.UrlResource(file.toURI());

        return ResponseEntity.ok()
                .header(
                        org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" +
                                java.net.URLEncoder.encode(
                                        post.getOriginalFileName(),
                                        java.nio.charset.StandardCharsets.UTF_8
                                ) + "\""
                )
                .body(resource);
    }
}