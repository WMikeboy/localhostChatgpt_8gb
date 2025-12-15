# localhostChatgpt_8gb
localhost a Chatgpt in RTX2080 max 8gb ram

Local Gemini Clone (Java + Ollama + ComfyUI)
這是一個在本地環境（Localhost）運行的全端 AI 聊天與生圖平台，模仿 Gemini 的體驗。專為 NVIDIA RTX 2080 (8GB VRAM) 硬體環境優化，透過 Java Spring Boot 進行資源調度，實現 LLM 與生圖模型的序列化運行。

📋 專案架構 (Architecture)
Frontend: React / Vue (待實作) - 用戶聊天介面。

Backend: Java (Spring Boot) - 核心控制器，負責 API 串接與 VRAM 顯存管理。

Database: MySQL (XAMPP) - 儲存對話歷史 (Context Window)。

LLM Engine: Ollama - 運行 qwen2.5:3b 模型 (輕量化中文支援)。

Image Engine: ComfyUI (Portable) - 運行 Z-Image Workflow + GGUF 模型 (低顯存優化)。

🛠️ 硬體與環境需求 (Prerequisites)
OS: Windows 10/11

GPU: NVIDIA RTX 2080 (8GB VRAM) 或同級顯卡

Software:

Java JDK 17+

Maven

XAMPP (MySQL)

Git

🚀 安裝與設定流程 (Installation & Setup)
1. 資料庫設定 (Database)
啟動 XAMPP MySQL，建立資料庫 local_gemini 與歷史紀錄表：

SQL

CREATE TABLE chat_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(255),
    role VARCHAR(50), -- 'user' or 'assistant'
    content TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
2. LLM 設定 (Ollama)
由於 8GB 顯存限制，我們使用 3B 模型以保留空間給生圖。

安裝 Ollama Desktop。

下載模型：

Bash

ollama pull qwen2.5:3b
測試服務：確保 http://localhost:11434 正常運作。

3. 生圖引擎設定 (ComfyUI Portable)
注意：必須使用 Portable 版本以支援 API 控制。

下載: 使用 ComfyUI_windows_portable_nvidia_cu121_or_cpu.7z。

依賴修復: 在 ComfyUI_windows_portable 目錄下開啟 CMD，安裝缺失模組：

DOS

.\python_embeded\python.exe -m pip install GitPython toml rich matrix-client requests
啟動檔修改: 編輯 run_nvidia_gpu.bat，確保內容如下（解決路徑與 API 監聽問題）：

程式碼片段

cd /d %~dp0
.\python_embeded\python.exe -s ComfyUI\main.py --windows-standalone-build --listen --enable-cors-header --preview-method none
pause
安裝插件:

git clone https://github.com/ltdrdata/ComfyUI-Manager.git 到 custom_nodes 資料夾。

啟動 ComfyUI，進入 Manager 安裝 ComfyUI-GGUF 及其他缺失節點。

解決安裝權限問題:

若遇 "Security Level" 報錯，暫時移除 .bat 中的 --listen 參數，重啟後安裝節點，裝完再加回去。

或修改 ComfyUI-Manager/config.ini 將 security_level 設為 weak。

4. Java 後端邏輯 (Backend Logic)
核心在於 "顯存序列化 (VRAM Serialization)"：

用戶輸入 -> Java 接收。

意圖判斷：

若為聊天 -> 呼叫 Ollama (qwen2.5:3b) -> 回傳文字。

若為畫圖 -> Java 發送 keep_alive: 0 給 Ollama (強制卸載 LLM) -> 呼叫 ComfyUI API -> 輪詢生成狀態 -> 回傳圖片 -> 重新喚醒 Ollama。

常見問題排除 (Troubleshooting)
Error: "The system cannot find the path specified" in ComfyUI

解法: 確保 .bat 檔第一行加上 cd /d %~dp0，且不要使用 Desktop Installer 版本。

Error: "ModuleNotFoundError: No module named 'xxx'"

解法: 使用 .\python_embeded\python.exe -m pip install xxx 手動安裝缺失的 Python 庫。

Ollama 回答變成英文或胡言亂語

解法: 確保程式碼中指定的模型是 qwen2.5:3b 而不是 llama3.2。

ComfyUI Manager 無法安裝節點 (Security Error)

解法: 因開啟 --listen 導致，需暫時關閉監聽或調整 Manager 設定檔的安全層級。
