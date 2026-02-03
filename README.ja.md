<div align="center">

# 🌌 Localhost ChatGPT (8GB VRAM Optimized)
### Java Spring Boot UI + Telegram Bot + Ollama + ComfyUI

[![English](https://img.shields.io/badge/Language-English-blue?style=for-the-badge)](README.md)
[![Traditional Chinese](https://img.shields.io/badge/Language-繁體中文-red?style=for-the-badge)](README.zh-TW.md)
[![Japanese](https://img.shields.io/badge/Language-日本語-green?style=for-the-badge)](README.ja.md)

</div>

## 📖 プロジェクト概要
**LocalhostChatGPT** は、**コンシューマー向けハードウェア（NVIDIA RTX 2080 8GB）** で動作するように設計された、プライバシー重視のフルスタックAIプラットフォームです。テキスト（Ollama）と画像生成（ComfyUI）を統合し、Google Geminiのようなマルチモーダル体験をローカルで再現します。

本バージョンでは、**Java Spring BootによるWeb UI** と **Telegram Bot** 機能を追加し、ブラウザやスマートフォンから手軽にローカルAIと対話できるようになりました。

### 🌟 主な機能
* **Web UI:** Java Spring Bootで構築されたシンプルでレスポンシブなチャット画面。
* **Telegram Bot:** Telegramアプリを通じて、外出先やスマホからローカルAIとチャット・画像生成が可能。
* **ハードウェア最適化:** GGUFとFP32強制演算により、RTX 2080におけるVRAM不足とNaNノイズ問題を解決。
* **ローカルファースト:** データは外部に送信されません。会話履歴はMySQLに保存されます。

### 📋 システム構成
* **Frontend:** HTML/JS / Telegram App
* **Backend:** **Java Spring Boot** (オーケストレーター, Telegram Botロジック, APIハンドラー)
* **Database:** MySQL (XAMPP) - 会話ログの保存。
* **AI Engine:**
    * **Text:** Ollama (`qwen2.5:3b`)
    * **Image:** ComfyUI Portable (Z-Image Turbo Workflow + GGUF)

---

## 🛠️ 必須環境
* **GPU:** NVIDIA RTX 2080 (8GB) または同等クラス。
* **Java:** JDK 17以上
* **サービス:** XAMPP (MySQL), Ollama, ComfyUI Portable。

---

## 🚀 インストールと設定

### 1. 外部サービスの準備
Javaアプリを起動する前に、バックグラウンドで以下のAIエンジンを起動してください。

#### A. データベース (MySQL)
1.  XAMPPでMySQLを起動します。
2.  `local_gemini` という名前のデータベースを作成します。
3.  `application.properties` に接続情報を設定します。

#### B. Ollama (LLM)
1.  `ollama pull qwen2.5:3b` を実行してモデルを準備します。
2.  ポート `11434` で待機していることを確認します。

#### C. ComfyUI (画像生成)
*RTX 2080最適化ガイド（別紙参照）に従ってください。*
1.  **重要:** ノイズ画像生成を防ぐため、必ず `--fp32-vae --force-fp32 --listen` オプションを付けて起動してください。
2.  ポート `8188` で待機していることを確認します。

### 2. Javaアプリケーションの設定
`src/main/resources/application.properties` を編集します：

```properties
# データベース設定
spring.datasource.url=jdbc:mysql://localhost:3306/local_gemini
spring.datasource.username=root
spring.datasource.password=

# Telegram Bot設定 (@BotFather から取得)
telegram.bot.username=あなたのBotユーザー名
telegram.bot.token=あなたのBotトークン

# AIサービス URL
api.ollama.url=http://localhost:11434
api.comfyui.url=http://localhost:8188
```

### 3. アプリケーションの起動
```bash
./mvnw spring-boot:run
```
* **Web UI:** ブラウザで `http://localhost:8080` にアクセス。
* **Telegram:** Botに対してメッセージを送信し、動作を確認してください。

---

## ⚠️ トラブルシューティング
* **生成画像がノイズ (QRコード状) になる:** ComfyUIの起動オプションに `--force-fp32` が含まれているか再確認してください。
* **Botが応答しない:** `application.properties` のTokenが正しいか、およびPCがTelegram APIサーバーに接続できる状態か確認してください。