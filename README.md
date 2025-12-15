Local Gemini Clone (Java + Ollama + ComfyUI)這是一個在本地環境（Localhost）運行的全端 AI 聊天與生圖平台，模仿 Gemini 的體驗。專為 NVIDIA RTX 2080 (8GB VRAM) 硬體環境優化，解決了顯存限制與依賴庫衝突問題。📋 專案架構 (Architecture)Frontend: React / Vue (待實作)Backend: Java (Spring Boot) - 核心控制器，負責 API 串接與 VRAM 顯存管理。Database: MySQL (XAMPP) - 儲存對話歷史。LLM Engine: Ollama (Desktop) - 運行 qwen2.5:3b (輕量化中文支援)。Image Engine: ComfyUI (Portable) - 運行 Z-Image Workflow + GGUF 模型。🛠️ 硬體與環境需求 (Prerequisites)OS: Windows 10/11GPU: NVIDIA RTX 2080 (8GB VRAM) 或同級顯卡Software:Java JDK 17+XAMPP (MySQL)7-Zip (用於解壓 ComfyUI)Git (用於手動安裝節點)🚀 安裝與設定流程 (Installation & Setup)1. 資料庫 (MySQL)啟動 XAMPP MySQL，建立資料庫 local_gemini。2. LLM 設定 (Ollama)由於 8GB 顯存限制，嚴格限制使用 3B 模型。安裝 Ollama Desktop 版本。下載模型：ollama pull qwen2.5:3b測試：瀏覽器開啟 http://localhost:11434 顯示 "Ollama is running"。3. 生圖引擎設定 (ComfyUI Portable) - 關鍵步驟⚠️ 注意：請勿下載 Installer/Desktop 版，必須使用 Portable 版以支援 API 開發。下載:下載 ComfyUI_windows_portable_nvidia_cu121_or_cpu.7z (約 1.4GB)。(請勿使用 cu126 或 cu128 版本，避免相容性問題)修復 Python 依賴庫 (Dependency Fix):解壓縮後，在 ComfyUI_windows_portable 資料夾開啟 CMD，執行以下指令修復 Manager 報錯：DOS.\python_embeded\python.exe -m pip install toml rich GitPython requests matrix-client
修復 GPU 驅動錯誤 (Torch Fix):若遇到 Torch not compiled with CUDA enabled 錯誤，請執行此指令強制重裝 GPU 版 Torch：DOS.\python_embeded\python.exe -m pip install torch torchvision torchaudio --index-url https://download.pytorch.org/whl/cu121 --force-reinstall
修改啟動腳本 (run_nvidia_gpu.bat):編輯 .bat 檔，用以下內容完全取代（解決路徑錯誤與 API 監聽）：程式碼片段cd /d %~dp0
.\python_embeded\python.exe -s ComfyUI\main.py --windows-standalone-build --listen --enable-cors-header --preview-method none
pause
解決 Manager 安裝權限問題:修改 ComfyUI/custom_nodes/ComfyUI-Manager/config.ini：Ini, TOML[default]
security_level = weak
(設為 weak 可解決開啟 --listen 後無法安裝節點的問題)安裝 GGUF 支援:啟動 ComfyUI，使用 Manager 安裝 ComfyUI-GGUF 節點，或手動 Git Clone 到 custom_nodes 資料夾。⚠️ 災難排除手冊 (Troubleshooting Guide)記錄了開發過程中遇到的所有坑與解法：錯誤訊息 (Error Message)原因 (Cause)解法 (Solution)System cannot find the path specifiedCMD 執行路徑錯誤或使用了 Desktop 版1. 改用 Portable 版2. .bat 第一行加上 cd /d %~dp0ModuleNotFoundError: No module named 'toml' / 'rich'Portable Python 缺少依賴庫執行 .\python_embeded\python.exe -m pip install toml rich ...AssertionError: Torch not compiled with CUDA enabled更新導致 PyTorch 變成 CPU 版執行 pip install ... --force-reinstall (詳見安裝步驟 3)Installation Error: Action not allowed with this security level開啟 API 監聽導致安全鎖定修改 ComfyUI-Manager/config.ini 將 security_level 改為 weakComfyUI-GGUF 節點顯示紅色 (Missing)缺少外掛節點使用 Manager 安裝 ComfyUI-GGUF
<h1>手動安裝 ComfyUI-GGUF 步驟：

進入節點資料夾： 進入 ComfyUI_windows_portable\ComfyUI\custom_nodes\。

開啟 CMD： 在網址列輸入 cmd 並按 Enter。

輸入下載指令： 複製貼上以下指令並執行：

Bash

git clone https://github.com/city96/ComfyUI-GGUF.git
等待完成： 當你看到資料夾裡多了一個 ComfyUI-GGUF 資料夾，就代表安裝成功了！</h1>
