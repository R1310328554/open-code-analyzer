/**
 * citation-utils.ts — 聊天引用标记解析：阿拉伯-印度数字归一化、索引提取与匹配正则。
 */

/** 将阿拉伯-印度/波斯数字（٠-٩、۰-۹）统一转为 ASCII 0-9。 */
export const normalizeCitationDigits = (text: string) => {
  if (!text) return text;
  return text.replace(/[٠-٩۰-۹]/g, (char) => {
    const code = char.charCodeAt(0);
    if (code >= 0x0660 && code <= 0x0669) {
      return String.fromCharCode(code - 0x0660 + 0x30);
    }
    if (code >= 0x06f0 && code <= 0x06f9) {
      return String.fromCharCode(code - 0x06f0 + 0x30);
    }
    return char;
  });
};

/** 从 [ID:n]、[n] 或纯数字字符串解析引用块索引，无效时返回 NaN。 */
export const parseCitationIndex = (value: string) => {
  const normalized = normalizeCitationDigits(value);
  const markerMatch = normalized.match(/\[(?:ID:)?(\d+)\]/);
  if (markerMatch) return Number(markerMatch[1]);
  if (/^\d+$/.test(normalized)) return Number(normalized);
  return Number.NaN;
};

/** 全局引用标记正则，支持 ASCII 与阿拉伯-印度/波斯数字。 */
export const citationMarkerReg =
  /\[(?:ID:)?([0-9\u0660-\u0669\u06F0-\u06F9]+)\]/g;
