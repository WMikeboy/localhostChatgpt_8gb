<div align="center">

# 🌌 Localhost ChatGPT (8GB VRAM 優化版)
### Java Spring Boot UI + Telegram Bot + Ollama + ComfyUI

[![English](https://img.shields.io/badge/Language-English-blue?style=for-the-badge)](README.md)
[![Traditional Chinese](https://img.shields.io/badge/Language-繁體中文-red?style=for-the-badge)](README.zh-TW.md)
[![Japanese](https://img.shields.io/badge/Language-日本語-green?style=for-the-badge)](README.ja.md)

</div>

## 📖 專案概述
**LocalhostChatGPT** 是一個專注於隱私的全端 AI 平台，旨在**消費級硬體 (NVIDIA RTX 2080 8GB)** 上運行。它整合了文字 (Ollama) 與 圖像生成 (ComfyUI)，模仿 Google Gemini 的多模態體驗。

此版本新增了 **Java Spring Boot Web UI** 與 **Telegram Bot** 整合，讓您可以透過網頁或手機隨時與本地 AI 互動。

### 🌟 核心功能
* **Web UI:** 基於 Java Spring Boot 構建的簡潔網頁聊天介面。
* **Telegram Bot:** 直接在 Telegram 上與您的本地 AI 聊天並生成圖片。
* **硬體優化:** 透過 GGUF 與強制 FP32 運算，解決 RTX 2080 的顯存瓶頸與 NaN 噪點問題。
* **本地優先:** 數據不出本機，對話記錄儲存於 MySQL。

### 📋 系統架構
* **Frontend:** HTML/JS / Telegram App
* **Backend:** **Java Spring Boot** (核心控制、Telegram Bot 邏輯、API 串接)
* **Database:** MySQL (XAMPP) - 儲存對話歷史。
* **AI Engine:**
    * **文字:** Ollama (`qwen2.5:3b`)
    * **圖像:** ComfyUI Portable (Z-Image Turbo Workflow + GGUF)

---

## 🛠️ 環境需求
* **GPU:** NVIDIA RTX 2080 (8GB) 或同級顯卡。
* **Java:** JDK 17+
* **服務:** XAMPP (MySQL), Ollama, ComfyUI Portable。

---

## 🚀 安裝與設定

### 1. 外部服務設定
啟動 Java 程式前，請確保以下 AI 引擎已在背景運行。

#### A. 資料庫 (MySQL)
1.  透過 XAMPP 啟動 MySQL。
2.  建立資料庫 `local_gemini`。
3.  在 `application.properties` 設定帳號密碼。

#### B. Ollama (LLM)
1.  執行 `ollama pull qwen2.5:3b`。
2.  確認運行於 port `11434`。

#### C. ComfyUI (生圖引擎)
*請參考先前的 RTX 2080 優化指南。*
1.  **重要:** 啟動參數務必包含 `--fp32-vae --force-fp32 --listen` 以避免黑圖或雜訊。
2.  確認運行於 port `8188`。

### 2. Java 應用程式設定
編輯 `src/main/resources/application.properties`：

```properties
# 資料庫設定
spring.datasource.url=jdbc:mysql://localhost:3306/local_gemini
spring.datasource.username=root
spring.datasource.password=

# Telegram Bot 設定 (請找 @BotFather 申請)
telegram.bot.username=你的機器人ID
telegram.bot.token=你的機器人Token

# AI 服務位址
api.ollama.url=http://localhost:11434
api.comfyui.url=http://localhost:8188
```

### 3. 啟動應用程式
```bash
./mvnw spring-boot:run
```
* **Web UI:** 瀏覽器打開 `http://localhost:8080`
* **Telegram:** 向您的機器人發送訊息即可開始。

---

## ⚠️ 疑難排解
* **生成的圖片是雜訊 (QR Code):** 檢查 ComfyUI 是否已加入 `--force-fp32` 參數。
* **機器人沒反應:** 檢查 `application.properties` 中的 Token 是否正確，並確認電腦網路能連接 Telegram API。