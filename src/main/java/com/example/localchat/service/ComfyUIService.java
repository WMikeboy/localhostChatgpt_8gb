package com.example.localchat.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets; // 使用標準 UTF-8
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class ComfyUIService {

    // ⚠️ 請確認路徑正確
    private static final String PYTHON_PATH = "D:\\ComfyUI_windows_portable_nvidia_cu121_or_cpu\\ComfyUI_windows_portable\\python_embeded\\python.exe";
    private static final String SCRIPT_PATH = "D:\\ComfyUI_windows_portable_nvidia_cu121_or_cpu\\ComfyUI_windows_portable\\api\\api_trigger.py";

    public String generateImage(String prompt) {
        System.out.println("🎨 Java 正在呼叫 Python 生圖...");
        StringBuilder fullLog = new StringBuilder();
        String generatedHtml = null; // 改名為 generatedHtml 比較明確

        try {
            List<String> command = new ArrayList<>();
            command.add(PYTHON_PATH);
            command.add(SCRIPT_PATH);
            command.add(prompt);

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.directory(new File(SCRIPT_PATH).getParentFile());
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            // 強制使用 UTF-8 讀取 Python 輸出
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[Python]: " + line);
                    fullLog.append(line).append("\n");

                    // 抓取關鍵字
                    if (line.contains("💾 圖片已儲存為:")) {
                        String[] parts = line.split("💾 圖片已儲存為:");
                        if (parts.length > 1) {
                            String fullPath = parts[1].trim();
                            String fileName = new File(fullPath).getName();
                            
                            // 這裡一定要組裝成 HTML
                            generatedHtml = String.format(
                                "<div style='text-align:center;'>" +
                                "<h3>🎨 生圖完成！</h3>" +
                                "<img src='/images/%s' style='max-width:800px; border-radius:10px; box-shadow: 0 4px 8px rgba(0,0,0,0.1);'>" +
                                "<p>Prompt: %s</p>" +
                                "</div>", 
                                fileName, prompt
                            );
                        }
                    }
                }
            }

            int exitCode = process.waitFor();
            
            if (exitCode == 0 && generatedHtml != null) {
                return generatedHtml; // ✅ 回傳 HTML
            } else {
                return "❌ 生圖失敗。詳細 Log:\n" + fullLog.toString();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "錯誤: " + e.getMessage();
        }
    }
}