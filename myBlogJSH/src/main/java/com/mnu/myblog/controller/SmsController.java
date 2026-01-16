package com.mnu.myblog.controller;

import java.util.Random;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.service.DefaultMessageService;

@Controller
public class SmsController {

    private final DefaultMessageService messageService;

    public SmsController(
            @Value("${coolsms.apikey}") String apiKey,
            @Value("${coolsms.apisecret}") String apiSecret,
            @Value("${coolsms.url}") String url) {

        this.messageService =
                NurigoApp.INSTANCE.initialize(apiKey, apiSecret, url);
    }

    @PostMapping("/sms/send")
    @ResponseBody
    public String sendSms(
            @RequestParam("phone") String phone,
            HttpSession session) {

        // 6자리 인증번호 생성
        int code = new Random().nextInt(900000) + 100000;

        // 세션 저장
        session.setAttribute("smsCode", String.valueOf(code));
        session.setAttribute("smsPhone", phone);
        session.setAttribute("smsTime", System.currentTimeMillis());

        // 메시지 생성
        Message message = new Message();
        message.setFrom("01039745335"); // 발신번호 (사전 등록 필수)
        message.setTo(phone);
        message.setText("[Jo's Blog] 인증번호는 " + code + " 입니다.");

        try {
            messageService.send(message);
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "fail";
        }
    }

    @PostMapping("/sms/verify")
    @ResponseBody
    public String verifySms(
            @RequestParam("phone") String phone,
            @RequestParam("code") String code,
            HttpSession session) {

        String savedCode = (String) session.getAttribute("smsCode");
        String savedPhone = (String) session.getAttribute("smsPhone");
        Long savedTime = (Long) session.getAttribute("smsTime");

        if (savedCode == null || savedPhone == null || savedTime == null) {
            return "expired";
        }

        // 3분 초과
        if (System.currentTimeMillis() - savedTime > 180000) {
            return "expired";
        }

        if (!savedPhone.equals(phone) || !savedCode.equals(code)) {
            return "fail";
        }

        session.setAttribute("smsVerified", true);
        return "success";
    }
}