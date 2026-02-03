<div align="center">

# 🌌 Localhost ChatGPT (8GB VRAM Optimized)
### Java Spring Boot UI + Telegram Bot + Ollama + ComfyUI

[![English](https://img.shields.io/badge/Language-English-blue?style=for-the-badge)](README.md)
[![Traditional Chinese](https://img.shields.io/badge/Language-繁體中文-red?style=for-the-badge)](README.zh-TW.md)
[![Japanese](https://img.shields.io/badge/Language-日本語-green?style=for-the-badge)](README.ja.md)

</div>

## 📖 Project Overview
**LocalhostChatGPT** is a privacy-focused, full-stack AI platform designed to run on **consumer hardware (NVIDIA RTX 2080 8GB)**. It mimics the Google Gemini multi-modal experience by integrating text (Ollama) and image generation (ComfyUI) into a unified interface.

This version features a **Java Spring Boot Web UI** and a **Telegram Bot** interface, allowing you to chat with your local AI from anywhere.

### 🌟 Key Features
* **Web UI:** A clean, responsive chat interface built with Java Spring Boot.
* **Telegram Bot:** Chat with your local AI and generate images directly via Telegram.
* **Hardware Optimized:** Solves VRAM bottlenecks and FP16 NaN issues on RTX 2080 using GGUF & FP32 forcing.
* **Local First:** No data leaves your machine. History is stored in MySQL.

### 📋 Architecture
* **Frontend:** HTML/JS / Telegram App
* **Backend:** **Java Spring Boot** (Orchestrator, Telegram Bot Logic, API Handler)
* **Database:** MySQL (XAMPP) - Conversation storage.
* **AI Engine:**
    * **Text:** Ollama (`qwen2.5:3b`)
    * **Image:** ComfyUI Portable (Z-Image Turbo Workflow + GGUF)

---

## 🛠️ Prerequisites
* **GPU:** NVIDIA RTX 2080 (8GB) or equivalent.
* **Java:** JDK 17+
* **Services:** XAMPP (MySQL), Ollama, ComfyUI Portable.

---

## 🚀 Installation & Setup

### 1. External Services Setup
Please ensure the AI engines are running before starting the Java app.

#### A. Database (MySQL)
1.  Start MySQL via XAMPP.
2.  Create a database named `local_gemini`.
3.  Configure your `application.properties` with your DB credentials.

#### B. Ollama (LLM)
1.  Run `ollama pull qwen2.5:3b`.
2.  Ensure it's listening on port `11434`.

#### C. ComfyUI (Image Gen)
*Follow the "RTX 2080 Optimization" guide (see previous docs or Wiki).*
1.  **Important:** Launch with `--fp32-vae --force-fp32 --listen` to prevent black images/noise.
2.  Ensure it's listening on port `8188`.

### 2. Java Application Configuration
Edit `src/main/resources/application.properties`:

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/local_gemini
spring.datasource.username=root
spring.datasource.password=

# Telegram Bot
telegram.bot.username=YOUR_BOT_USERNAME
telegram.bot.token=YOUR_BOT_TOKEN

# AI Services
api.ollama.url=http://localhost:11434
api.comfyui.url=http://localhost:8188
```

### 3. Running the App
```bash
./mvnw spring-boot:run
```
* **Web UI:** Access `http://localhost:8080`
* **Telegram:** Start a chat with your bot to begin.

---

## ⚠️ Troubleshooting
* **Image is Noise/QR Code:** Check if ComfyUI is running with `--force-fp32`.
* **Bot not responding:** Verify the Token in `application.properties` and ensure your PC can connect to Telegram API.