package com.mnu.myblog.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PostLikeMapper {

    /* ===============================
       ❤️ 좋아요 존재 여부 (중복 방지)
       - post_id + user_id 기준
       =============================== */
    int existsLike(@Param("postId") Long postId,
                   @Param("userId") String userId);

    /* ===============================
       ❤️ 좋아요 추가
       =============================== */
    void insertLike(@Param("postId") Long postId,
                    @Param("userId") String userId);

    /* ===============================
       ❌ 좋아요 삭제 (미사용)
       =============================== */
    void deleteLike(@Param("postId") Long postId,
                    @Param("userId") String userId);
}