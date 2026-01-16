package com.mnu.myblog.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mnu.myblog.domain.CommentDTO;

@Mapper
public interface CommentMapper {

    /* ================= 댓글 목록 ================= */

    // 게시글별 댓글 + 대댓글
    List<CommentDTO> selectByPostId(@Param("postId") Long postId);

    // 게시글별 댓글 + 대댓글 (삭제 포함, 관리자/상세용)
    List<CommentDTO> selectByPostIdWithDeleted(@Param("postId") Long postId);

    // 관리자용 전체 댓글 (페이징 없음)
    List<CommentDTO> selectAll();

    // 관리자용 전체 댓글 (페이징)
    List<CommentDTO> selectAllPaged(@Param("limit") int limit,
                                    @Param("offset") int offset);

    /* ================= 댓글 작성 ================= */

    void insert(CommentDTO comment);

    /* ================= 댓글 삭제 ================= */

    // 작성자 삭제 (SOFT DELETE)
    int delete(@Param("commentId") Long commentId,
               @Param("userId") String userId);

    /* ================= 관리자 댓글 삭제 ================= */

    // 관리자 SOFT DELETE
    int deleteByAdminSoft(@Param("commentId") Long commentId);

    // 관리자 HARD DELETE (부모 + 대댓글)
    void deleteParentHardByAdmin(@Param("commentId") Long commentId);

    // 관리자 HARD DELETE (대댓글 단독)
    void deleteChildHardByAdmin(@Param("commentId") Long commentId);

    /* ================= 카운트 ================= */

    // 일반(유저 기준) 총 댓글 수
    int selectTotalCount();

    // ✅ 관리자 전용 전체 댓글 수 (HARD DELETE 반영)
    int selectTotalCountAdmin();

    int selectTodayCount();

    /* ================= 🔴 댓글 신고 ================= */

    // 중복 신고 체크
    int checkIfReported(@Param("commentId") Long commentId,
                        @Param("userId") String userId);

    // 댓글 신고 (reason 포함)
    int reportComment(@Param("commentId") Long commentId,
                      @Param("userId") String userId,
                      @Param("reason") String reason);

    /* ================= 🚨 관리자 신고 댓글 ================= */

    // 신고 댓글 전체 목록 (비페이징)
    List<CommentDTO> selectReportedComments();

    // 신고 댓글 총 개수 (페이징용)
    int selectReportedCommentsCount();

    // 신고 댓글 목록 (페이징)
    List<CommentDTO> selectReportedCommentsPaged(@Param("limit") int limit,
                                                 @Param("offset") int offset);

    /* ================= 신고 처리 ================= */

    // 신고 기록 삭제
    void deleteReportsByCommentId(@Param("commentId") Long commentId);
    
    void restoreByAdmin(@Param("commentId") Long commentId);
}