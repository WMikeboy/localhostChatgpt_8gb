import logging
import asyncio
import requests
import re
import os
from dotenv import load_dotenv
from telegram import Update
from telegram.ext import ApplicationBuilder, ContextTypes, CommandHandler, MessageHandler, filters

# 載入 .env (如果沒有使用 .env，直接把 Token 填在下面也可以)
load_dotenv()
TG_TOKEN = os.getenv("TG_BOT_TOKEN", "你的_TOKEN_填在這裡")

# 設定 Java 後端地址
JAVA_BASE_URL = "http://localhost:60002"
JAVA_API_URL = f"{JAVA_BASE_URL}/api/bot/chat"

try:
    ADMIN_ID = int(os.getenv("ALLOWED_USER_ID"))
except (TypeError, ValueError):
    print("⚠️ 警告：未設定 ALLOWED_USER_ID，目前 Bot 為公開狀態！")
    ADMIN_ID = None

logging.basicConfig(
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    level=logging.INFO
)

async def start(update: Update, context: ContextTypes.DEFAULT_TYPE):
    await context.bot.send_message(
        chat_id=update.effective_chat.id,
        text="👋 連線成功！我是你的 Local Chat 機器人。試試看叫我「畫一張貓」？"
    )

def call_java_backend(user_id, text):
    """呼叫 Java API 並回傳結果"""
    try:
        payload = {"userId": str(user_id), "message": text}
        # 畫圖可能需要較長時間，Timeout 設定 120 秒
        response = requests.post(JAVA_API_URL, json=payload, timeout=120)
        
        if response.status_code == 200:
            data = response.json()
            return data.get("reply", "")
        else:
            return f"❌ 伺服器錯誤: {response.status_code}"
    except Exception as e:
        return f"❌ 連線失敗: {str(e)}"

async def handle_message(update: Update, context: ContextTypes.DEFAULT_TYPE):
    user_id = update.effective_user.id
    chat_id = update.effective_chat.id

    # =========== [新增] 安全門禁檢查 ===========
    if ADMIN_ID and user_id != ADMIN_ID:
        print(f"🛑 攔截到未授權訪問: User ID {user_id}")
        # 直接回覆拒絕，並結束函式，不讓它往下跑去呼叫 Java
        await context.bot.send_message(chat_id=chat_id, text="⛔️ Access Denied: 你沒有權限使用此 Bot。")
        return
    # =========================================

    user_text = update.message.text

    # 顯示「輸入中...」或「上傳圖片中...」
    await context.bot.send_chat_action(chat_id=chat_id, action="typing")

    loop = asyncio.get_running_loop()
    ai_reply = await loop.run_in_executor(None, call_java_backend, chat_id, user_text)

    # --- 圖片處理邏輯 (這部分沒問題，維持原樣) ---
    img_match = re.search(r'<img\s+[^>]*src="([^"]+)"', ai_reply)
    
    if img_match:
        image_path = img_match.group(1)
        # 組合完整的 URL (確保 Java 回傳的路徑開頭有 /，如果沒有可能要手動補)
        full_image_url = f"{JAVA_BASE_URL}{image_path}"
        
        await context.bot.send_message(chat_id=chat_id, text="🎨 圖片生成完畢，正在傳送...")
        await context.bot.send_chat_action(chat_id=chat_id, action="upload_photo")
        
        try:
            await context.bot.send_photo(chat_id=chat_id, photo=full_image_url)
        except Exception as e:
            await context.bot.send_message(chat_id=chat_id, text=f"圖片傳送失敗，請檢查路徑: {full_image_url}")
            
    else:
        # 過濾 HTML 標籤
        clean_text = re.sub(r'<[^>]+>', '', ai_reply).strip()
        if not clean_text:
            clean_text = ai_reply
            
        await context.bot.send_message(chat_id=chat_id, text=clean_text)

if __name__ == '__main__':
    if "你的_TOKEN" in TG_TOKEN:
        print("⚠️ 請先設定 Telegram Bot Token！")
        exit(1)

    app = ApplicationBuilder().token(TG_TOKEN).build()
    app.add_handler(CommandHandler('start', start))
    app.add_handler(MessageHandler(filters.TEXT & (~filters.COMMAND), handle_message))
    
    print("🚀 Bot 啟動中... 等待訊息...")
    app.run_polling()
