package com.mnu.myblog.controller;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;

import com.mnu.myblog.domain.UserDTO;
import com.mnu.myblog.service.UserService;

@Controller
public class ProfileController {

    private final UserService userService;

    // ✅ 実際のアップロードパス（WebConfigと一致させる）
    private static final String PROFILE_UPLOAD_DIR = "C:/upload/profile/";

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    /* =========================
       プロフィール画像アップロード
    ========================= */
    @PostMapping("/profile/upload")
    public String uploadProfile(@RequestParam("profileImage") MultipartFile file,
                                HttpSession session,
                                RedirectAttributes ra) throws IOException {

        // 🔒 ログインチェック
        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) {
            ra.addFlashAttribute("toastMsg", "ログイン後にプロフィール画像を変更できます。");
            ra.addFlashAttribute("toastType", "error");
            return "redirect:/";
        }

        // ❌ ファイル未選択
        if (file == null || file.isEmpty()) {
            ra.addFlashAttribute("toastMsg", "画像ファイルを選択してください。");
            ra.addFlashAttribute("toastType", "error");
            return "redirect:/";
        }

        // 📁 アップロードフォルダ作成
        File uploadDir = new File(PROFILE_UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // 🔑 UUIDファイル名生成
        String originalName = file.getOriginalFilename();
        String ext = originalName.substring(originalName.lastIndexOf("."));
        String savedFileName = UUID.randomUUID() + ext;

        // 💾 ファイル保存
        File saveFile = new File(uploadDir, savedFileName);
        file.transferTo(saveFile);

        // 🌐 Webアクセス用パス（DB保存値）
        String profilePath = "/profile/" + savedFileName;

        // 🗄 DB更新
        userService.updateProfileImage(loginUser.getUserId(), profilePath);

        // 🔄 セッション同期
        loginUser.setProfileImage(profilePath);
        session.setAttribute("loginUser", loginUser);

        // ✅ 成功メッセージ
        ra.addFlashAttribute("toastMsg", "プロフィール画像を変更しました。");
        ra.addFlashAttribute("toastType", "success");

        return "redirect:/";
    }

    /* =========================
       ステータスメッセージ更新
    ========================= */
    @PostMapping("/profile/message")
    public String updateProfileMessage(@RequestParam("message") String message,
                                       HttpSession session) {

        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/";
        }

        String trimmed = message != null ? message.trim() : "";

        userService.updateProfileMessage(loginUser.getUserId(), trimmed);

        loginUser.setProfileMessage(trimmed);
        session.setAttribute("loginUser", loginUser);

        return "redirect:/admin";
    }
}
