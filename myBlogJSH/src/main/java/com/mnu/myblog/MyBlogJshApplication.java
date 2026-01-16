package com.mnu.myblog;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.mnu.myblog.mapper")
@EnableScheduling   // ✅ 스케줄러 필수 설정 (이거 없으면 자동 삭제 절대 안 됨)
public class MyBlogJshApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyBlogJshApplication.class, args);
        
        
    }
}