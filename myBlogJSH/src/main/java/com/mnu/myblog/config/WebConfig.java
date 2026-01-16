package com.mnu.myblog.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.mnu.myblog.interceptor.LoginCheckInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(new LoginCheckInterceptor())
                .addPathPatterns(
                        "/mypage/**",
                        "/post/**",
                        "/admin/**"
                )
                .excludePathPatterns(
                        "/login",
                        "/register",
                        "/sms/**",
                        "/css/**",
                        "/js/**",
                        "/images/**",

                        // ✅ 업로드 이미지 경로는 전부 제외
                        "/profile/**",
                        "/notice/**"
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        registry.addResourceHandler("/profile/**")
                .addResourceLocations("file:///C:/upload/profile/");

        registry.addResourceHandler("/notice-img/**")
        		.addResourceLocations("file:///C:/upload/notice/");
    }
}