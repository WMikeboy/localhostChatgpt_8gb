🌌 Local Gemini Clone (Java + Ollama + ComfyUI)
這是一個在本地環境（Localhost）運行的全端 AI 聊天與生圖平台，目標是模仿 Gemini 的多模態體驗。
專案核心針對 NVIDIA RTX 2080 (8GB VRAM) 硬體環境進行深度優化，透過 GGUF 量化技術與 FP32 強制運算，解決了舊架構顯卡在運算新模型時的顯存限制與數值溢位（NaN）問題。

📋 專案架構 (Architecture)
Frontend: React / Vue (待實作)
Backend: Java (Spring Boot) - 核心控制器，負責 API 串接、Prompt 處理與 VRAM 顯存調度。
Database: MySQL (XAMPP) - 儲存對話歷史記錄。
LLM Engine: Ollama (Desktop) - 運行 qwen2.5:3b (輕量化中文支援，低顯存佔用)。
Image Engine: ComfyUI (Portable) - 運行 Z-Image Turbo Workflow + GGUF 模型 (解決顯存瓶頸)。

🛠️ 硬體與環境需求 (Prerequisites)
OS: Windows 10 / 11
GPU: NVIDIA RTX 2080 (8GB VRAM) 或同級 RTX 20/30 系列顯卡
Software:
Java JDK 17+
XAMPP (提供 MySQL 服務)
7-Zip (用於解壓 ComfyUI)
Git (用於手動安裝節點)

🚀 安裝與設定流程 (Installation & Setup)
1. 資料庫 (MySQL)
啟動 XAMPP 的 MySQL 模組。
建立一個新的資料庫，命名為 local_gemini。
2. LLM 設定 (Ollama)
由於 8GB 顯存限制，生圖時 VRAM 吃緊，故 LLM 嚴格限制使用 3B 參數模型。
安裝 Ollama Desktop 版本。
開啟 CMD 下載模型：ollama pull qwen2.5:3b
測試：瀏覽器開啟 http://localhost:11434，應顯示 "Ollama is running"。
3. 生圖引擎設定 (ComfyUI Portable) - ★ 關鍵步驟
⚠️ 注意：請勿使用 Installer/Desktop 版，必須使用 Portable 版以支援 API 開發與 Python 環境控制。
3.1 下載與解壓
下載 ComfyUI_windows_portable_nvidia_cu121_or_cpu.7z (約 1.4GB)。
(建議使用 cu121 版本，避免與 RTX 20 系列顯卡發生相容性問題)
3.2 修復 Python 依賴庫 (Dependency Fix)
解壓縮後，在 ComfyUI_windows_portable 資料夾內開啟 CMD，依序執行以下指令：
修復 Manager 與基礎功能：
DOS
.\python_embeded\python.exe -m pip install toml rich GitPython requests matrix-client


修復 Qwen (LLM) 支援 (缺少此庫會導致 Prompt 執行失敗)：
DOS
.\python_embeded\python.exe -m pip install sentencepiece transformers protobuf


3.3 修復 GPU 驅動 (Torch Fix - 選用)
若啟動時遇到 Torch not compiled with CUDA enabled 錯誤，請執行此指令強制重裝 GPU 版 Torch：
DOS
.\python_embeded\python.exe -m pip install torch torchvision torchaudio --index-url https://download.pytorch.org/whl/cu121 --force-reinstall

3.4 修改啟動腳本 (run_nvidia_gpu.bat) - ★最重要
編輯 .bat 檔，用以下內容完全取代。
此腳本加入了 --fp32-vae --force-fp32，強制 RTX 2080 使用單精度運算，徹底解決生成雜訊圖 (QR Code) 的問題。
程式碼片段
cd /d %~dp0
.\python_embeded\python.exe -s ComfyUI\main.py --windows-standalone-build --listen --enable-cors-header --preview-method none --lowvram --fp32-vae --force-fp32
pause

3.5 解決 Manager 權限問題
修改 ComfyUI/custom_nodes/ComfyUI-Manager/config.ini：
Ini, TOML
[default]
security_level = weak
# 設為 weak 可解決開啟 --listen 後無法安裝/移除節點的問題

3.6 安裝 GGUF 支援
啟動 ComfyUI，使用 Manager 搜尋並安裝 ComfyUI-GGUF 節點 (作者: City96)。
手動安裝 ComfyUI-GGUF 步驟：
進入節點資料夾： 進入 ComfyUI_windows_portable\ComfyUI\custom_nodes\。
開啟 CMD： 在網址列輸入 cmd 並按 Enter。
輸入下載指令： 複製貼上以下指令並執行：
git clone https://github.com/city96/ComfyUI-GGUF.git 等待完成： 當你看到資料夾裡多了一個 ComfyUI-GGUF 資料夾，就代表安裝成功了！

📂 模型放置規則 (Model Setup)
為確保 Workflow 正常運作，請將 GGUF 模型檔案依照以下規則放置：
模型類型
檔案名稱範例
目標資料夾路徑
備註
UNET (生圖)
z_image_turbo...gguf
ComfyUI\models\unet\
選擇 SDXL Turbo 類模型
CLIP (LLM)
Qwen3-4B...gguf
ComfyUI\models\clip\
注意：Qwen 必須放這裡
VAE
ae.safetensors
ComfyUI\models\vae\
標準 SDXL VAE


⚠️ 災難排除手冊 (Troubleshooting Guide)
記錄了開發過程中遇到的所有坑與最終解法：
錯誤訊息 / 現象 (Error/Symptom)
原因 (Cause)
解法 (Solution)
生成圖片全是雜訊 (QR Code)
[關鍵] PyTorch 2.5 + RTX 2080 在半精度 (FP16) 運算時發生數值溢位 (NaN)。
啟動參數必須加入 --fp32-vae --force-fp32 (詳見安裝步驟 3.4)。
Prompt execution failed
Python 環境缺少 sentencepiece 庫，導致 Qwen 無法 Tokenize。
執行 pip install sentencepiece transformers protobuf。
Requested to load Lumina2
ComfyUI-GGUF 外掛誤判模型架構。
這是誤報。只要有加 --force-fp32，即便顯示此訊息也能正常出圖，無需理會。
System cannot find the path specified
CMD 執行路徑錯誤，或腳本未定位到當前目錄。
在 .bat 第一行加上 cd /d %~dp0。
Installation Error: Action not allowed
開啟 --listen (API 模式) 導致安全鎖定。
修改 ComfyUI-Manager/config.ini 將 security_level 改為 weak。
ComfyUI-GGUF 節點顯示紅色
缺少外掛節點或載入失敗。
1. 確認已安裝 ComfyUI-GGUF。
2. 確認 Python 依賴庫已修復。

