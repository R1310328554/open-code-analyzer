/**
 * llm-cache.ts — LLM 模型列表 localStorage 缓存：5 分钟 TTL，供租户参数注入复用。
 */

/** 缓存条目：模型列表数据与写入时间戳。 */
interface LlmCache {
  data: Record<string, any>;
  timestamp: number;
}

/** localStorage 键名。 */
const CACHE_KEY = 'ragflow_llm_list_cache';
/** 缓存有效期（毫秒），默认 5 分钟。 */
const CACHE_DURATION = 5 * 60 * 1000; // 5 minutes

/** 读取未过期的 LLM 列表缓存，过期或异常时清除并返回 null。 */
export function getCachedLlmList(): Record<string, any> | null {
  try {
    const cached = localStorage.getItem(CACHE_KEY);
    if (!cached) return null;

    const parsed: LlmCache = JSON.parse(cached);
    const now = Date.now();

    // Check if cache is expired
    if (now - parsed.timestamp > CACHE_DURATION) {
      clearLlmCache();
      return null;
    }

    return parsed.data;
  } catch (error) {
    console.error('Error getting cached LLM list:', error);
    clearLlmCache();
    return null;
  }
}

/** 将 LLM 列表写入 localStorage 并附带当前时间戳。 */
export function setCachedLlmList(data: Record<string, any>): void {
  try {
    const cache: LlmCache = {
      data,
      timestamp: Date.now(),
    };
    localStorage.setItem(CACHE_KEY, JSON.stringify(cache));
  } catch (error) {
    console.error('Error setting cached LLM list:', error);
  }
}

/** 移除 localStorage 中的 LLM 列表缓存。 */
export function clearLlmCache(): void {
  try {
    localStorage.removeItem(CACHE_KEY);
  } catch (error) {
    console.error('Error clearing LLM cache:', error);
  }
}

/** 判断是否存在有效（未过期）的 LLM 列表缓存。 */
export function isLlmListCached(): boolean {
  return getCachedLlmList() !== null;
}
