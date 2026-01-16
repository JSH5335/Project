package com.mnu.myblog.controller;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mnu.myblog.domain.AdminLogType;
import com.mnu.myblog.domain.UserDTO;
import com.mnu.myblog.service.AdminLogService;
import com.mnu.myblog.service.UserService;

@Controller
public class UserController {

    private final UserService userService;
    private final AdminLogService adminLogService;

    public UserController(UserService userService,
                          AdminLogService adminLogService) {
        this.userService = userService;
        this.adminLogService = adminLogService;
    }

    /* ================= 会員登録 ================= */

    @GetMapping("/register")
    public String registerForm() {
        return "register";
    }

    @PostMapping("/register")
    public String register(UserDTO user,
                           HttpSession session,
                           RedirectAttributes ra) {

        Boolean smsVerified = (Boolean) session.getAttribute("smsVerified");
        if (smsVerified == null || !smsVerified) {
            ra.addFlashAttribute("toastMsg", "電話番号の認証が必要です。");
            ra.addFlashAttribute("toastType", "error");
            return "redirect:/register";
        }

        if (userService.isDuplicatedId(user.getUserId())) {
            ra.addFlashAttribute("toastMsg", "既に使用されているIDです。");
            ra.addFlashAttribute("toastType", "error");
            return "redirect:/register";
        }

        userService.register(user);
        session.removeAttribute("smsVerified");

        ra.addFlashAttribute("toastMsg", "会員登録が完了しました。ログインしてください。");
        ra.addFlashAttribute("toastType", "success");

        return "redirect:/login";
    }

    /* ================= ログイン ================= */

    @GetMapping("/login")
    public String loginForm(
            @RequestParam(name = "error", required = false) String error,
            RedirectAttributes ra) {

        if ("loginRequired".equals(error)) {
            ra.addFlashAttribute("toastMsg", "ログインが必要です。");
            ra.addFlashAttribute("toastType", "error");
        }

        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam(name = "userId", required = false) String userId,
                        @RequestParam(name = "userPw", required = false) String userPw,
                        HttpSession session,
                        RedirectAttributes ra) {

        if (userId == null || userId.isBlank()
                || userPw == null || userPw.isBlank()) {

            ra.addFlashAttribute("toastMsg", "IDとパスワードを入力してください。");
            ra.addFlashAttribute("toastType", "error");
            return "redirect:/login";
        }

        UserDTO user = userService.login(userId, userPw);
        if (user == null) {
            ra.addFlashAttribute("toastMsg", "IDまたはパスワードが正しくありません。");
            ra.addFlashAttribute("toastType", "error");
            return "redirect:/login";
        }

        session.setAttribute("loginUser", user);

        ra.addFlashAttribute("toastMsg", "ログインしました。");
        ra.addFlashAttribute("toastType", "success");

        return "redirect:/";
    }

    /* ================= ログアウト ================= */

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    /* =================================================
       ✏️ プロ필 상태 메시지 수정 (엔터 저장)
    ================================================= */

    @PostMapping("/user/profile-message")
    public String updateProfileMessage(@RequestParam("profileMessage") String profileMessage,
                                       HttpSession session,
                                       RedirectAttributes ra) {

        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");

        if (loginUser == null) {
            ra.addFlashAttribute("toastMsg", "ログインが必要です。");
            ra.addFlashAttribute("toastType", "error");
            return "redirect:/login";
        }

        userService.updateProfileMessage(loginUser.getUserId(), profileMessage);

        // 🔥 세션 즉시 갱신
        loginUser.setProfileMessage(profileMessage);
        session.setAttribute("loginUser", loginUser);

        ra.addFlashAttribute("toastMsg", "ステータスメッセージを保存しました。");
        ra.addFlashAttribute("toastType", "success");

        return "redirect:/";
    }

    /* =================================================
       🔐 会員 탈퇴
    ================================================= */

    @PostMapping("/user/withdraw")
    public String withdraw(@RequestParam("userPw") String userPw,
                           HttpSession session,
                           RedirectAttributes ra) {

        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");

        if (loginUser == null) {
            ra.addFlashAttribute("toastMsg", "ログインが必要です。");
            ra.addFlashAttribute("toastType", "error");
            return "redirect:/login";
        }

        UserDTO verifiedUser = userService.login(loginUser.getUserId(), userPw);

        if (verifiedUser == null) {
            ra.addFlashAttribute("toastMsg", "パスワードが正しくありません。");
            ra.addFlashAttribute("toastType", "error");
            return "redirect:/";
        }

        userService.withdraw(loginUser.getUserId());

        adminLogService.writeLog(
                loginUser.getUserId(),
                AdminLogType.USER_WITHDRAW,
                "self"
        );

        session.invalidate();

        ra.addFlashAttribute("toastMsg", "退会処理が完了しました。");
        ra.addFlashAttribute("toastType", "success");

        return "redirect:/";
    }

    /* ================= 관리자 기능 ================= */

    @PostMapping("/admin/users/role")
    public String changeUserRole(@RequestParam("userId") String userId,
                                 @RequestParam("role") String role,
                                 HttpSession session,
                                 RedirectAttributes ra) {

        UserDTO admin = (UserDTO) session.getAttribute("loginUser");

        if (admin == null || !"ADMIN".equals(admin.getRole())) {
            ra.addFlashAttribute("toastMsg", "管理者権限が必要です。");
            ra.addFlashAttribute("toastType", "error");
            return "redirect:/login";
        }

        userService.changeUserRole(userId, role);

        adminLogService.writeLog(
                admin.getUserId(),
                AdminLogType.USER_ROLE_UPDATE,
                "user#" + userId
        );

        ra.addFlashAttribute("toastMsg", "ユーザー権限を変更しました。");
        ra.addFlashAttribute("toastType", "success");

        return "redirect:/admin/users";
    }

    @PostMapping("/admin/users/ban")
    public String banUser(@RequestParam("userId") String userId,
                          @RequestParam("reason") String reason,
                          HttpSession session,
                          RedirectAttributes ra) {

        UserDTO admin = (UserDTO) session.getAttribute("loginUser");

        if (admin == null || !"ADMIN".equals(admin.getRole())) {
            ra.addFlashAttribute("toastMsg", "管理者権限が必要です。");
            ra.addFlashAttribute("toastType", "error");
            return "redirect:/login";
        }

        userService.banUser(userId, reason);

        adminLogService.writeLog(
                admin.getUserId(),
                AdminLogType.USER_BAN,
                "user#" + userId
        );

        ra.addFlashAttribute("toastMsg", "会員を停止しました。");
        ra.addFlashAttribute("toastType", "warning");

        return "redirect:/admin/users";
    }

    @PostMapping("/admin/users/unban")
    public String unbanUser(@RequestParam("userId") String userId,
                            HttpSession session,
                            RedirectAttributes ra) {

        UserDTO admin = (UserDTO) session.getAttribute("loginUser");

        if (admin == null || !"ADMIN".equals(admin.getRole())) {
            ra.addFlashAttribute("toastMsg", "管理者権限が必要です。");
            ra.addFlashAttribute("toastType", "error");
            return "redirect:/login";
        }

        userService.unbanUser(userId);

        adminLogService.writeLog(
                admin.getUserId(),
                AdminLogType.USER_UNBAN,
                "user#" + userId
        );

        ra.addFlashAttribute("toastMsg", "会員停止を解除しました。");
        ra.addFlashAttribute("toastType", "success");

        return "redirect:/admin/users";
    }
}