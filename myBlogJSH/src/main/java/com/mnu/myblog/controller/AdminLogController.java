package com.mnu.myblog.controller;

import java.util.List;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mnu.myblog.domain.AdminLogDTO;
import com.mnu.myblog.domain.UserDTO;
import com.mnu.myblog.service.AdminLogService;

@Controller
@RequestMapping("/admin/logs")
public class AdminLogController {

    private final AdminLogService adminLogService;

    public AdminLogController(AdminLogService adminLogService) {
        this.adminLogService = adminLogService;
    }

    /* ===============================
       管理者ログ一覧
    =============================== */
    @GetMapping
    public String logList(HttpSession session,
                          Model model,
                          RedirectAttributes ra) {

        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");

        // 🔐 管理者権限チェック
        if (loginUser == null || !"ADMIN".equals(loginUser.getRole())) {
            ra.addFlashAttribute("toastMsg", "管理者権限が必要です。");
            ra.addFlashAttribute("toastType", "error");
            return "redirect:/login";
        }

        List<AdminLogDTO> logList = adminLogService.getAllLogs();

        // ✅ STEP 2: target 문구 가공
        for (AdminLogDTO log : logList) {
            String target = log.getTarget();

            if (target == null) continue;

            if (target.startsWith("post#")) {
                log.setTarget(
                        "投稿 #" + target.replace("post#", "")
                );
            } else if (target.startsWith("comment#")) {
                log.setTarget(
                        "コメント #" + target.replace("comment#", "")
                );
            } else if (target.startsWith("notice#")) {
                log.setTarget(
                        "お知らせ #" + target.replace("notice#", "")
                );
            } else if (target.startsWith("user#")) {
                log.setTarget(
                        "ユーザー " + target.replace("user#", "")
                );
            }
        }

        model.addAttribute("logList", logList);
        return "admin/logs";
    }

    /* ===============================
       管理者ログ 全削除
    =============================== */
    @PostMapping("/delete-all")
    public String deleteAllLogs(RedirectAttributes ra) {

        adminLogService.deleteAllLogs();

        ra.addFlashAttribute("toastMsg", "すべての管理者ログを削除しました。");
        ra.addFlashAttribute("toastType", "error"); // ✅ 빨간 토스트로 변경

        return "redirect:/admin/logs";
    }

    /* ===============================
       管理者ログ 単件削除
    =============================== */
    @PostMapping("/delete")
    public String deleteLog(@RequestParam("logId") Long logId,
                            RedirectAttributes ra) {

        adminLogService.deleteLogById(logId);

        ra.addFlashAttribute("toastMsg", "管理者ログを削除しました。");
        ra.addFlashAttribute("toastType", "success");

        return "redirect:/admin/logs";
    }
}