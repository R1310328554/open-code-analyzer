/**
 * text-direction.ts — 文本方向（RTL/LTR）检测：阿拉伯语、希伯来语、波斯语等 Unicode 范围。
 * RTL (Right-to-Left) text direction utilities
 * Supports Arabic, Hebrew, Persian/Farsi, Urdu, and other RTL scripts
 */

/** RTL 书写系统 Unicode 码点区间表。 */
const RTL_RANGES: [number, number][] = [
  [0x0600, 0x06ff], // Arabic
  [0x0750, 0x077f], // Arabic Supplement
  [0x08a0, 0x08ff], // Arabic Extended-A
  [0xfb50, 0xfdff], // Arabic Presentation Forms-A
  [0xfe70, 0xfeff], // Arabic Presentation Forms-B
  [0x0590, 0x05ff], // Hebrew
  [0xfb1d, 0xfb4f], // Hebrew Presentation Forms
  [0x0700, 0x074f], // Syriac
  [0x0780, 0x07bf], // Thaana (Maldivian)
  [0x0840, 0x085f], // Mandaic
  [0x0860, 0x086f], // Syriac Supplement
];

/** 判断字符码点是否落在 RTL Unicode 区间。 */
const isRTLCharCode = (charCode: number): boolean => {
  return RTL_RANGES.some(
    ([start, end]) => charCode >= start && charCode <= end,
  );
};

/**
 * 扫描首个强方向字符（字母，跳过数字/标点/空白），返回 rtl/ltr/neutral。
 */
export const getTextDirection = (text: string): 'rtl' | 'ltr' | 'neutral' => {
  if (!text) return 'neutral';

  for (const char of text) {
    const code = char.charCodeAt(0);

    // Skip whitespace, numbers, and common punctuation
    if (
      code <= 0x40 || // Control chars, digits, basic punctuation
      (code >= 0x5b && code <= 0x60) || // [ \ ] ^ _ `
      (code >= 0x7b && code <= 0x7f) // { | } ~ DEL
    ) {
      continue;
    }

    // Check if RTL
    if (isRTLCharCode(code)) {
      return 'rtl';
    }

    // If we found a non-RTL letter, it's LTR
    // Latin, Greek, Cyrillic, etc.
    if (
      (code >= 0x41 && code <= 0x5a) || // A-Z
      (code >= 0x61 && code <= 0x7a) || // a-z
      (code >= 0x00c0 && code <= 0x024f) || // Latin Extended
      (code >= 0x0370 && code <= 0x03ff) || // Greek
      (code >= 0x0400 && code <= 0x04ff) // Cyrillic
    ) {
      return 'ltr';
    }
  }

  return 'neutral';
};

/** 文本是否包含任意 RTL 字符（用于混合内容检测）。 */
export const containsRTL = (text: string): boolean => {
  if (!text) return false;

  for (const char of text) {
    if (isRTLCharCode(char.charCodeAt(0))) {
      return true;
    }
  }
  return false;
};

/** 首强方向字符是否为 RTL（即整体按 RTL 处理）。 */
export const isRTL = (text: string): boolean => {
  return getTextDirection(text) === 'rtl';
};

/** 返回 HTML dir 属性值：rtl/ltr，neutral 时为 auto。 */
export const getDirAttribute = (text: string): 'rtl' | 'ltr' | 'auto' => {
  const direction = getTextDirection(text);
  return direction === 'neutral' ? 'auto' : direction;
};
