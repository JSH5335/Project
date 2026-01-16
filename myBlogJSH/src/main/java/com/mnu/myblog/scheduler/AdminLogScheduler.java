package com.mnu.myblog.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.mnu.myblog.service.AdminLogService;

@Component
public class AdminLogScheduler {

    private final AdminLogService adminLogService;

    public AdminLogScheduler(AdminLogService adminLogService) {
        this.adminLogService = adminLogService;
    }

    // ⏰ 10분마다 실행 (원하면 조절 가능)
    @Scheduled(fixedRate = 10 * 60 * 1000)
    public void cleanOldLogs() {
        adminLogService.deleteOldLogs();
    }
}