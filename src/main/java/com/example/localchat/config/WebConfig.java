package com.example.localchat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // ⚠️ 請修改為您 Python 腳本產生 outputs 資料夾的「絕對路徑」
    // 注意：結尾必須要有斜線 "/"，前面要加 "file:"
    private static final String OUTPUT_DIR = "file:D:/ComfyUI_windows_portable_nvidia_cu121_or_cpu/ComfyUI_windows_portable/api/outputs/";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 將網址 /images/** 對應到硬碟資料夾
        registry.addResourceHandler("/images/**")
                .addResourceLocations(OUTPUT_DIR);
    }
}