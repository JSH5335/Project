package com.mnu.myblog.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mnu.myblog.domain.PostDTO;

@Mapper
public interface PostMapper {

    /* ================= 投稿作成 ================= */
    void insertPost(PostDTO post);

    /* ================= 投稿一覧 ================= */
    List<PostDTO> selectPostList();

    /* ================= 投稿一覧（ページング + 検索） ================= */
    List<PostDTO> selectPostListPaged(
            @Param("limit") int limit,
            @Param("offset") int offset,
            @Param("keyword") String keyword
    );

    /* ================= 投稿数（検索含む） ================= */
    int selectPostCount(
            @Param("keyword") String keyword
    );

    /* ================= 管理者 投稿一覧（既存） ================= */
    List<PostDTO> selectPostListAdmin();

    /* ==================================================
       🔥 管理者 投稿一覧（ページング）
    ================================================== */

    /* 管理者 投稿一覧（LIMIT / OFFSET） */
    List<PostDTO> selectPostListAdminPaged(
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    /* 管理者 投稿総数 */
    int selectPostAdminCount();

    /* ==================================================
       🔍 管理者 投稿一覧（検索 + 状態フィルター + ページング）
    ================================================== */
    List<PostDTO> selectPostListAdminPagedWithFilter(
            @Param("limit") int limit,
            @Param("offset") int offset,
            @Param("keyword") String keyword,
            @Param("status") String status
    );

    /* ==================================================
       🔢 管理者 投稿総数（検索 + 状態フィルター）
    ================================================== */
    int selectPostAdminCountWithFilter(
            @Param("keyword") String keyword,
            @Param("status") String status
    );

    /* ================= 投稿詳細 ================= */
    PostDTO selectPostById(Long postId);

    /* ================= 投稿修正 ================= */
    void updatePost(PostDTO post);

    /* ================= 投稿修正（ファイル含む） ================= */
    void updatePostWithFile(PostDTO post);

    /* ================= 🗑 管理者 削除 / 復元 ================= */
    void deletePostAdmin(Long postId);
    void restorePostAdmin(Long postId);

    /* ================= 👁 非公開 / 公開 ================= */
    void hidePost(Long postId);
    void showPost(Long postId);

    /* ================= 👀 閲覧数 ================= */
    void increaseViewCount(Long postId);

    /* ================= ❤️ いいね ================= */
    void increaseLikeCount(Long postId);
    void decreaseLikeCount(Long postId);

    /* ================= 📌 固定 ================= */
    void pinPost(Long postId);
    void unpinPost(Long postId);

    /* ================= 🧹 削除済み投稿 一括整理 (HARD DELETE) ================= */
    void cleanupDeletedPosts();

    /* ================= 🔥 人気投稿 TOP 3 ================= */
    List<PostDTO> selectPopularTop3();
}