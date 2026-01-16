package com.mnu.myblog.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mnu.myblog.domain.CommentDTO;
import com.mnu.myblog.mapper.CommentMapper;

@Service
@Transactional
public class CommentService {

    private final CommentMapper commentMapper;

    public CommentService(CommentMapper commentMapper) {
        this.commentMapper = commentMapper;
    }

    /* ================= 댓글 목록 ================= */

    // 게시글별 댓글 + 대댓글 (구조화)
    public List<CommentDTO> getCommentList(Long postId) {

        List<CommentDTO> allComments = commentMapper.selectByPostId(postId);

        if (allComments == null || allComments.isEmpty()) {
            return new ArrayList<>();
        }

        List<CommentDTO> parentComments = allComments.stream()
                .filter(c -> c.getParentId() == null)
                .collect(Collectors.toList());

        for (CommentDTO parent : parentComments) {

            List<CommentDTO> replies = allComments.stream()
                    .filter(c -> parent.getCommentId().equals(c.getParentId()))
                    .collect(Collectors.toList());

            parent.setReplyList(replies != null ? replies : new ArrayList<>());
        }

        return parentComments;
    }

    /* ================= 댓글 목록 (삭제 포함) ================= */

    // 게시글별 댓글 + 대댓글 (삭제된 댓글 포함)
    public List<CommentDTO> getCommentListWithDeleted(Long postId) {

        List<CommentDTO> allComments =
                commentMapper.selectByPostIdWithDeleted(postId);

        if (allComments == null || allComments.isEmpty()) {
            return new ArrayList<>();
        }

        List<CommentDTO> parentComments = allComments.stream()
                .filter(c -> c.getParentId() == null)
                .collect(Collectors.toList());

        for (CommentDTO parent : parentComments) {

            List<CommentDTO> replies = allComments.stream()
                    .filter(c -> parent.getCommentId().equals(c.getParentId()))
                    .collect(Collectors.toList());

            parent.setReplyList(replies != null ? replies : new ArrayList<>());
        }

        return parentComments;
    }

    /* ================= 댓글 작성 ================= */

    public void write(CommentDTO comment) {
        commentMapper.insert(comment);
    }

    /* ================= 작성자 댓글 삭제 (SOFT DELETE) ================= */

    public boolean deleteByUser(Long commentId, String userId) {
        return commentMapper.delete(commentId, userId) > 0;
    }

    /* ================= 관리자 댓글 삭제 ================= */

    // 관리자 SOFT DELETE
    public void deleteByAdminSoft(Long commentId) {
        commentMapper.deleteByAdminSoft(commentId);
    }

    // 컨트롤러 호환용
    public void softDeleteByAdmin(Long commentId) {
        deleteByAdminSoft(commentId);
    }

    /* ================= 관리자 댓글 복구 ================= */

    public void restoreByAdmin(Long commentId) {
        commentMapper.restoreByAdmin(commentId);
    }

    /* ================= 관리자 댓글 완전 삭제 (HARD DELETE) ================= */

    public void deleteByAdminHard(Long commentId, Long parentId) {

        if (parentId == null) {
            commentMapper.deleteParentHardByAdmin(commentId);
        } else {
            commentMapper.deleteChildHardByAdmin(commentId);
        }
    }

    public void deleteByAdminHard(Long commentId) {
        deleteByAdminHard(commentId, null);
    }

    /* ================= 관리자 전체 댓글 ================= */

    public List<CommentDTO> getAllComments() {
        return commentMapper.selectAll();
    }

    public List<CommentDTO> getAllCommentsPaged(int limit, int offset) {
        return commentMapper.selectAllPaged(limit, offset);
    }

    // 일반(유저 기준)
    public int getTotalCount() {
        return commentMapper.selectTotalCount();
    }

    // ✅ 관리자 전용 (HARD DELETE 반영)
    public int getTotalCountAdmin() {
        return commentMapper.selectTotalCountAdmin();
    }

    public int getTodayCount() {
        return commentMapper.selectTodayCount();
    }

    /* ================= 🔴 댓글 신고 ================= */

    public boolean report(Long commentId, String userId, String reason) {

        if (commentMapper.checkIfReported(commentId, userId) > 0) {
            return false;
        }

        return commentMapper.reportComment(commentId, userId, reason) > 0;
    }

    /* ================= 🔴 관리자 신고 댓글 처리 ================= */

    public List<CommentDTO> getReportedComments() {
        return commentMapper.selectReportedComments();
    }

    public int getReportedCommentsCount() {
        return commentMapper.selectReportedCommentsCount();
    }

    public List<CommentDTO> getReportedCommentsPaged(int limit, int offset) {
        return commentMapper.selectReportedCommentsPaged(limit, offset);
    }

    public void deleteReportedComment(Long commentId, Long parentId) {

        if (parentId == null) {
            commentMapper.deleteParentHardByAdmin(commentId);
        } else {
            commentMapper.deleteChildHardByAdmin(commentId);
        }

        commentMapper.deleteReportsByCommentId(commentId);
    }

    public void ignoreReport(Long commentId) {
        commentMapper.deleteReportsByCommentId(commentId);
    }
}
