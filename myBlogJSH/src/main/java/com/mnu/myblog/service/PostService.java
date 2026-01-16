package com.mnu.myblog.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mnu.myblog.domain.PostDTO;
import com.mnu.myblog.mapper.PostMapper;

@Service
@Transactional
public class PostService {

    private final PostMapper postMapper;

    public PostService(PostMapper postMapper) {
        this.postMapper = postMapper;
    }

    /* ================= 投稿作成 ================= */
    public void write(PostDTO post) {
        postMapper.insertPost(post);
    }

    /* ================= 投稿一覧（一般） ================= */
    public List<PostDTO> getPostList() {
        return postMapper.selectPostList();
    }

    /* ================= 投稿一覧（ページング + 検索） ================= */
    public List<PostDTO> getPostListPaged(int limit, int offset, String keyword) {
        return postMapper.selectPostListPaged(limit, offset, keyword);
    }

    /* ================= 投稿数（検索含む） ================= */
    public int getPostCount(String keyword) {
        return postMapper.selectPostCount(keyword);
    }

    /* ================= 管理者 投稿一覧（全体） ================= */
    public List<PostDTO> getPostListAdmin() {
        return postMapper.selectPostListAdmin();
    }

    /* ================= 管理者 投稿一覧（ページング） ================= */
    public List<PostDTO> getPostListAdminPaged(int limit, int offset) {
        return postMapper.selectPostListAdminPaged(limit, offset);
    }

    /* ================= 管理者 投稿総数 ================= */
    public int getPostAdminCount() {
        return postMapper.selectPostAdminCount();
    }

    /* ================= 管理者 投稿一覧（検索 + 状態） ================= */
    public List<PostDTO> getPostListAdminPagedWithFilter(
            int limit,
            int offset,
            String keyword,
            String status
    ) {
        return postMapper.selectPostListAdminPagedWithFilter(
                limit, offset, keyword, status
        );
    }

    /* ================= 管理者 投稿総数（検索 + 状態） ================= */
    public int getPostAdminCountWithFilter(String keyword, String status) {
        return postMapper.selectPostAdminCountWithFilter(keyword, status);
    }

    /* ================= 投稿詳細 ================= */
    public PostDTO getPost(Long postId) {
        return postMapper.selectPostById(postId);
    }

    /* ================= 投稿修正 ================= */
    public void update(PostDTO post) {
        updatePostWithFile(post);
    }

    /* ================= 投稿修正（ファイル含む） ================= */
    public void updatePostWithFile(PostDTO post) {
        postMapper.updatePostWithFile(post);
    }

    /* ================= 🗑 管理者 削除 / 復元 ================= */
    public void deleteByAdmin(Long postId) {
        postMapper.deletePostAdmin(postId);
    }

    public void restoreByAdmin(Long postId) {
        postMapper.restorePostAdmin(postId);
    }

    /* ================= 👁 公開 / 非公開 ================= */
    public void hide(Long postId) {
        postMapper.hidePost(postId);   // isVisible = false
    }

    public void show(Long postId) {
        postMapper.showPost(postId);   // isVisible = true
    }

    /* ================= 👀 閲覧数 ================= */
    public void increaseViewCount(Long postId) {
        postMapper.increaseViewCount(postId);
    }

    /* ================= 📌 固定 ================= */
    public void pin(Long postId) {
        postMapper.pinPost(postId);
    }

    public void unpin(Long postId) {
        postMapper.unpinPost(postId);
    }

    /* ==================================================
       ✅ 관리자 전용 토글 메서드
    ================================================== */

    /* 📌 고정 ON / OFF */
    public void togglePin(Long postId) {
        PostDTO post = postMapper.selectPostById(postId);
        if (post.isPinned()) {
            unpin(postId);
        } else {
            pin(postId);
        }
    }

    /* 👁 공개 / 비공개 */
    public void toggleHide(Long postId) {
        PostDTO post = postMapper.selectPostById(postId);
        if (post.isVisible()) {
            hide(postId);
        } else {
            show(postId);
        }
    }

    /* 🗑 관리자 삭제 (SOFT DELETE) */
    public void deletePostByAdmin(Long postId) {
        deleteByAdmin(postId);
    }

    /* ================= 🧹 삭제된 게시글 정리 ================= */
    public void cleanupDeletedPosts() {
        postMapper.cleanupDeletedPosts();
    }

    /* ================= 🔥 人気投稿 TOP 3 ================= */
    public List<PostDTO> getPopularTop3() {
        return postMapper.selectPopularTop3();
    }
}