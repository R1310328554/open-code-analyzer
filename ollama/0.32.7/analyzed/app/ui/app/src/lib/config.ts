// API 与 Ollama 客户端的基础 URL 配置
// API configuration
// 开发模式下桌面后端 API 地址
const DEV_API_URL = "http://127.0.0.1:3001";

// fetch 调用的 API 前缀（生产环境可为相对路径）
// Base URL for fetch API calls (can be relative in production)
export const API_BASE = import.meta.env.DEV ? DEV_API_URL : "";

// Ollama 浏览器客户端所需的完整 origin
// Full host URL for Ollama client (needs full origin in production)
export const OLLAMA_HOST = import.meta.env.DEV
  ? DEV_API_URL
  : window.location.origin;

/** ollama.com 站点根 URL，用于头像等相对路径补全。 */
export const OLLAMA_DOT_COM =
  import.meta.env.VITE_OLLAMA_DOT_COM_URL || "https://ollama.com";
