package com.mnu.myblog.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mnu.myblog.mapper.PostLikeMapper;
import com.mnu.myblog.mapper.PostMapper;

@Service
@Transactional
public class PostLikeService {

    private final PostLikeMapper postLikeMapper;
    private final PostMapper postMapper;

    public PostLikeService(PostLikeMapper postLikeMapper,
                           PostMapper postMapper) {
        this.postLikeMapper = postLikeMapper;
        this.postMapper = postMapper;
    }

    /* ================= ❤️ 좋아요 (하루 1회 제한) ================= */

    /**
     * 게시글 좋아요
     * - 하루 1번만 가능
     * - 중복 INSERT 방지 (DB 에러 차단)
     *
     * @return true = 좋아요 성공, false = 이미 좋아요 상태
     */
    public boolean likeOncePerDay(Long postId, String userId) {

        // 🔒 이미 좋아요 기록이 존재하면 즉시 차단
        int exists = postLikeMapper.existsLike(postId, userId);
        if (exists > 0) {
            return false;
        }

        // ❤️ 좋아요 기록
        postLikeMapper.insertLike(postId, userId);

        // 📈 게시글 좋아요 수 증가
        postMapper.increaseLikeCount(postId);

        return true;
    }

    /**
     * ✅ 이미 좋아요 했는지 여부 (조회용)
     * - 게시글 상세 GET에서 사용
     */
    @Transactional(readOnly = true)
    public boolean existsLike(Long postId, String userId) {
        return postLikeMapper.existsLike(postId, userId) > 0;
    }
}