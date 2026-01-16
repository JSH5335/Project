package com.mnu.myblog.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.mnu.myblog.domain.NoticeDTO;

public interface NoticeMapper {

    /* ================= ユーザー ================= */

    // お知らせ一覧（ユーザー）
    List<NoticeDTO> selectNoticeList(Map<String, Object> param);

    // お知らせ詳細
    NoticeDTO selectNotice(@Param("noticeId") Long noticeId);

    // ⭐ メインページ最新お知らせ 3件
    List<NoticeDTO> selectLatestNotices();

    /* ================= 管理者 ================= */

    // 管理者お知らせ一覧
    List<NoticeDTO> selectAdminNoticeList(Map<String, Object> param);

    // お知らせ作成
    void insertNotice(NoticeDTO notice);

    // お知らせ削除
    void deleteNotice(@Param("noticeId") Long noticeId);

    // 表示 / 非表示 切替
    void toggleVisible(@Param("noticeId") Long noticeId);

    // 📌 固定 ON / OFF（0 / 1 明示）
    void updatePinned(@Param("noticeId") Long noticeId,
                      @Param("pinned") int pinned);

    /* ================= カウント ================= */

    // 閲覧数増加
    void increaseViews(@Param("noticeId") Long noticeId);

    // ✔ チェック数増加
    void increaseCheckCount(@Param("noticeId") Long noticeId);

    /* ================= 統計 ================= */

    Map<String, Integer> getNoticeStats();
}
