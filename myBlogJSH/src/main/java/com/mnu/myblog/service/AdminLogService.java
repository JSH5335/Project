package com.mnu.myblog.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mnu.myblog.domain.AdminLogDTO;
import com.mnu.myblog.domain.AdminLogType;
import com.mnu.myblog.mapper.AdminLogMapper;

@Service
public class AdminLogService {

    private final AdminLogMapper adminLogMapper;

    public AdminLogService(AdminLogMapper adminLogMapper) {
        this.adminLogMapper = adminLogMapper;
    }

    /* ================= 관리자 로그 저장 ================= */
    public void writeLog(String adminId, AdminLogType type, String target) {

        AdminLogDTO log = new AdminLogDTO();
        log.setAdminId(adminId);
        log.setAction(type.name());
        log.setTarget(target);

        adminLogMapper.insertAdminLog(log);
    }

    /* ================= 관리자 로그 조회 ================= */
    public List<AdminLogDTO> getAllLogs() {
        return adminLogMapper.findAllLogs();
    }

    /* ================= 🔥 6시간 지난 로그 삭제 ================= */
    public void deleteOldLogs() {
        adminLogMapper.deleteLogsOlderThan6Hours();
    }

    /* ================= 🔥 전체 로그 삭제 ================= */
    public void deleteAllLogs() {
        adminLogMapper.deleteAllLogs();
    }

    /* ================= 🔥 개별 로그 삭제 ================= */
    public void deleteLogById(Long logId) {
        adminLogMapper.deleteLogById(logId);
    }
}
