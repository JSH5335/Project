package com.mnu.myblog.controller;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mnu.myblog.domain.NoticeDTO;
import com.mnu.myblog.domain.UserDTO;
import com.mnu.myblog.domain.AdminLogType;
import com.mnu.myblog.service.NoticeService;
import com.mnu.myblog.service.AdminLogService;

@Controller
@RequestMapping("/admin/notice")
public class AdminNoticeController {

    private final NoticeService noticeService;
    private final AdminLogService adminLogService;

    // ✅ アップロードパス
    private static final String UPLOAD_DIR = "C:/upload/notice/";

    public AdminNoticeController(NoticeService noticeService,
                                 AdminLogService adminLogService) {
        this.noticeService = noticeService;
        this.adminLogService = adminLogService;
    }

    /* ================= 管理者一覧 ================= */

    @GetMapping("/list")
    public String list(@RequestParam(value = "keyword", required = false) String keyword,
                       HttpSession session,
                       Model model,
                       RedirectAttributes ra) {

        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");

        if (loginUser == null || !"ADMIN".equals(loginUser.getRole())) {
            ra.addFlashAttribute("toastMsg", "管理者権限が必要です。");
            ra.addFlashAttribute("toastType", "error");
            return "redirect:/login";
        }

        Map<String, Object> param = new HashMap<>();
        param.put("keyword", keyword);

        model.addAttribute("noticeList", noticeService.getAdminNoticeList(param));
        model.addAttribute("keyword", keyword);

        return "admin/notice_list";
    }

    /* ================= お知らせ作成 ================= */

    @GetMapping("/write")
    public String writeForm(HttpSession session, RedirectAttributes ra) {

        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");

        if (loginUser == null || !"ADMIN".equals(loginUser.getRole())) {
            ra.addFlashAttribute("toastMsg", "管理者権限が必要です。");
            ra.addFlashAttribute("toastType", "error");
            return "redirect:/login";
        }

        return "admin/notice_write";
    }

    @PostMapping("/write")
    public String write(NoticeDTO notice,
                        @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                        HttpSession session,
                        RedirectAttributes ra) {

        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");

        if (loginUser == null || !"ADMIN".equals(loginUser.getRole())) {
            ra.addFlashAttribute("toastMsg", "管理者権限が必要です。");
            ra.addFlashAttribute("toastType", "error");
            return "redirect:/login";
        }

        // ✅ 기본 상태 명시 (중요)
        notice.setVisible(1);
        notice.setPinned(0);

        /* ===== 画像アップロード ===== */
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                File uploadDir = new File(UPLOAD_DIR);
                if (!uploadDir.exists()) uploadDir.mkdirs();

                String originalName = imageFile.getOriginalFilename();
                String ext = originalName.substring(originalName.lastIndexOf("."));
                String savedName = UUID.randomUUID() + ext;

                File savedFile = new File(uploadDir, savedName);
                imageFile.transferTo(savedFile);

                // ✅ 공지 이미지 URL (컨트롤러 경로와 분리)
                notice.setImagePath("/notice-img/" + savedName);

            } catch (IOException e) {
                ra.addFlashAttribute("toastMsg", "画像のアップロードに失敗しました。");
                ra.addFlashAttribute("toastType", "error");
                return "redirect:/admin/notice/write";
            }
        }

        noticeService.write(notice);

        // ✅ noticeId null 안전 처리
        String target = notice.getNoticeId() != null
                ? "notice#" + notice.getNoticeId()
                : "notice(created)";

        adminLogService.writeLog(
                loginUser.getUserId(),
                AdminLogType.NOTICE_CREATE,
                target
        );

        ra.addFlashAttribute("toastMsg", "お知らせを登録しました。");
        ra.addFlashAttribute("toastType", "success");

        return "redirect:/admin/notice/list";
    }

    /* ================= 表示切替 ================= */

    @PostMapping("/toggle-visible/{noticeId}")
    public String toggleVisible(@PathVariable("noticeId") Long noticeId,
                                HttpSession session,
                                RedirectAttributes ra) {

        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");

        if (loginUser == null || !"ADMIN".equals(loginUser.getRole())) {
            ra.addFlashAttribute("toastMsg", "管理者権限が必要です。");
            ra.addFlashAttribute("toastType", "error");
            return "redirect:/login";
        }

        noticeService.toggleVisible(noticeId);

        adminLogService.writeLog(
                loginUser.getUserId(),
                AdminLogType.NOTICE_VISIBLE,
                "notice#" + noticeId
        );

        return "redirect:/admin/notice/list";
    }

    /* ================= PIN 切替 ================= */

    @PostMapping("/toggle-pinned/{noticeId}")
    public String togglePinned(@PathVariable("noticeId") Long noticeId,
                               HttpSession session,
                               RedirectAttributes ra) {

        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");

        if (loginUser == null || !"ADMIN".equals(loginUser.getRole())) {
            ra.addFlashAttribute("toastMsg", "管理者権限が必要です。");
            ra.addFlashAttribute("toastType", "error");
            return "redirect:/login";
        }

        boolean pinned = noticeService.togglePinned(noticeId);

        adminLogService.writeLog(
                loginUser.getUserId(),
                pinned ? AdminLogType.NOTICE_PIN : AdminLogType.NOTICE_UNPIN,
                "notice#" + noticeId
        );

        return "redirect:/admin/notice/list";
    }

    /* ================= 削除 ================= */

    @PostMapping("/delete/{noticeId}")
    public String delete(@PathVariable("noticeId") Long noticeId,
                         HttpSession session,
                         RedirectAttributes ra) {

        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");

        if (loginUser == null || !"ADMIN".equals(loginUser.getRole())) {
            ra.addFlashAttribute("toastMsg", "管理者権限が必要です。");
            ra.addFlashAttribute("toastType", "error");
            return "redirect:/login";
        }

        noticeService.delete(noticeId);

        adminLogService.writeLog(
                loginUser.getUserId(),
                AdminLogType.NOTICE_DELETE,
                "notice#" + noticeId
        );

        ra.addFlashAttribute("toastMsg", "お知らせを削除しました。");
        ra.addFlashAttribute("toastType", "success");

        return "redirect:/admin/notice/list";
    }
}