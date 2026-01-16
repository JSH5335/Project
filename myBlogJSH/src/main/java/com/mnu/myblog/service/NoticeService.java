package com.mnu.myblog.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.mnu.myblog.domain.NoticeDTO;
import com.mnu.myblog.mapper.NoticeMapper;

@Service
public class NoticeService {

    private final NoticeMapper noticeMapper;

    public NoticeService(NoticeMapper noticeMapper) {
        this.noticeMapper = noticeMapper;
    }

    /* ================= ユーザー ================= */

    public List<NoticeDTO> getNoticeList(Map<String, Object> param) {
        return noticeMapper.selectNoticeList(param);
    }

    public NoticeDTO getNotice(Long noticeId) {
        return noticeMapper.selectNotice(noticeId);
    }

    public List<NoticeDTO> getLatestNotices() {
        return noticeMapper.selectLatestNotices();
    }

    /* ================= 管理者 ================= */

    public List<NoticeDTO> getAdminNoticeList(Map<String, Object> param) {
        return noticeMapper.selectAdminNoticeList(param);
    }

    public void write(NoticeDTO notice) {
        noticeMapper.insertNotice(notice);
    }

    public void delete(Long noticeId) {
        noticeMapper.deleteNotice(noticeId);
    }

    public void toggleVisible(Long noticeId) {
        noticeMapper.toggleVisible(noticeId);
    }

    /**
     * 📌 お知らせ固定切替
     * @return true  : 固定 ON
     *         false : 固定 OFF
     */
    public boolean togglePinned(Long noticeId) {

        NoticeDTO notice = noticeMapper.selectNotice(noticeId);
        if (notice == null) {
            return false;
        }

        // 🔥 핵심: int 기준으로 상태 계산
        int nextPinned = (notice.getPinned() == 1) ? 0 : 1;

        // 🔥 상태 지정 방식으로 업데이트
        noticeMapper.updatePinned(noticeId, nextPinned);

        return nextPinned == 1;
    }

    /* ================= カウント ================= */

    public void increaseViews(Long noticeId) {
        noticeMapper.increaseViews(noticeId);
    }

    public void increaseCheckCount(Long noticeId) {
        noticeMapper.increaseCheckCount(noticeId);
    }

    /* ================= 統計 ================= */

    public Map<String, Integer> getNoticeStats() {
        return noticeMapper.getNoticeStats();
    }
}
