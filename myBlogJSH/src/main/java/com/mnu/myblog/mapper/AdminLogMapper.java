package com.mnu.myblog.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mnu.myblog.domain.AdminLogDTO;

@Mapper
public interface AdminLogMapper {

    /* ================= 관리자 로그 ================= */

    // 관리자 로그 저장
    int insertAdminLog(AdminLogDTO log);

    // 관리자 로그 전체 조회
    List<AdminLogDTO> findAllLogs();

    // 🔥 6시간 지난 로그 자동 삭제
    int deleteLogsOlderThan6Hours();

    // 🔥 전체 로그 삭제 (버튼용)
    int deleteAllLogs();

    // 🔥 단일 로그 삭제 (선택 삭제용)
    int deleteLogById(@Param("logId") Long logId);
}