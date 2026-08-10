/**
 * 懒加载单例 Ollama 浏览器客户端，host 来自 config。
 */
import { Ollama } from "ollama/browser";
import { OLLAMA_HOST } from "./config";

/** 底层 Ollama 实例，首次访问属性时创建。 */
let _ollamaClient: Ollama | null = null;

/** Proxy 包装：按需实例化并正确绑定方法 this。 */
export const ollamaClient = new Proxy({} as Ollama, {
  get(_target, prop) {
    if (!_ollamaClient) {
      _ollamaClient = new Ollama({
        host: OLLAMA_HOST,
      });
    }
    const value = _ollamaClient[prop as keyof Ollama];
    return typeof value === "function" ? value.bind(_ollamaClient) : value;
  },
});
