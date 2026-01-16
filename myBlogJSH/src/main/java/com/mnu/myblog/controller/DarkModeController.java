package com.mnu.myblog.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@Controller
public class DarkModeController {

    @PostMapping("/darkmode/toggle")
    public String toggleDarkMode(
            HttpSession session,
            @RequestHeader("Referer") String referer
    ) {
        Boolean dark = (Boolean) session.getAttribute("darkMode");
        session.setAttribute("darkMode", dark == null ? true : !dark);
        return "redirect:" + referer;
    }
}