package com.mnu.myblog.controller;

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mnu.myblog.domain.NoticeDTO;
import com.mnu.myblog.domain.UserDTO;
import com.mnu.myblog.service.NoticeService;

@Controller
@RequestMapping("/notice")
public class NoticeController {

    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    /* ================= 공지 목록 ================= */

    @GetMapping("/list")
    public String list(
            @RequestParam(value = "keyword", required = false) String keyword,
            Model model) {

        Map<String, Object> param = new HashMap<>();
        param.put("keyword", keyword);

        model.addAttribute("noticeList", noticeService.getNoticeList(param));
        model.addAttribute("keyword", keyword);

        return "notice/list";
    }

    /* ================= 공지 상세 (조회수 + 체크 여부) ================= */

    @GetMapping("/view/{noticeId}")
    public String view(
            @PathVariable("noticeId") Long noticeId,
            HttpSession session,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model) {

        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");

        /* ===== 조회수 : 로그인 + 하루 1회 ===== */
        if (loginUser != null) {
            String cookieName = "notice_view_" + noticeId;
            boolean viewed = false;

            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie c : cookies) {
                    if (cookieName.equals(c.getName())) {
                        viewed = true;
                        break;
                    }
                }
            }

            if (!viewed) {
                noticeService.increaseViews(noticeId);

                Cookie viewCookie = new Cookie(cookieName, "true");
                viewCookie.setMaxAge(60 * 60 * 24);
                viewCookie.setPath("/");
                response.addCookie(viewCookie);
            }
        }

        /* ===== 체크 여부 판단 (유저별 쿠키 기준) ===== */
        boolean checked = false;

        if (loginUser != null) {
            String checkCookieName =
                    "notice_check_" + noticeId + "_" + loginUser.getUserId();

            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie c : cookies) {
                    if (checkCookieName.equals(c.getName())) {
                        checked = true;
                        break;
                    }
                }
            }
        }

        NoticeDTO notice = noticeService.getNotice(noticeId);

        model.addAttribute("notice", notice);
        model.addAttribute("loginUser", loginUser);
        model.addAttribute("checked", checked);

        return "notice/view";
    }

    /* ================= ✔ 체크 (GET / POST 허용) ================= */

    @RequestMapping(
            value = "/check/{noticeId}",
            method = {RequestMethod.GET, RequestMethod.POST}
    )
    public String check(
            @PathVariable("noticeId") Long noticeId,
            HttpSession session,
            HttpServletRequest request,
            HttpServletResponse response,
            RedirectAttributes ra) {

        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");

        if (loginUser == null) {
            ra.addFlashAttribute("toastMsg", "로그인이 필요합니다.");
            ra.addFlashAttribute("toastType", "error");
            return "redirect:/login";
        }

        String cookieName =
                "notice_check_" + noticeId + "_" + loginUser.getUserId();
        boolean checked = false;

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (cookieName.equals(c.getName())) {
                    checked = true;
                    break;
                }
            }
        }

        if (!checked) {
            noticeService.increaseCheckCount(noticeId);

            Cookie checkCookie = new Cookie(cookieName, "true");
            checkCookie.setMaxAge(60 * 60 * 24);
            checkCookie.setPath("/");
            response.addCookie(checkCookie);
        } else {
            ra.addFlashAttribute("toastMsg", "이미 체크하셨습니다.");
            ra.addFlashAttribute("toastType", "info");
        }

        return "redirect:/notice/view/" + noticeId;
    }
}