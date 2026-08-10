/**
 * common-util.ts — 前端通用工具：FormData 检测、snake_case 转换、URL 参数、数字/颜色/文件大小格式化等。
 */

import { LLMFactory } from '@/constants/llm';
import { IFactory } from '@/interfaces/database/llm';
import isObject from 'lodash/isObject';
import snakeCase from 'lodash/snakeCase';

/** 类型守卫：判断值是否为 FormData 实例。 */
export const isFormData = (data: unknown): data is FormData => {
  return data instanceof FormData;
};

const excludedFields: Array<string | RegExp> = [
  'img2txt_id',
  'mcpServers',
  'image_base64',
];

/** 判断字段名是否在 snake_case 转换排除列表中。 */
const isExcludedField = (key: string) => {
  return excludedFields.some((excl) =>
    excl instanceof RegExp ? excl.test(key) : excl === key,
  );
};

/** 递归将普通对象键名转为 snake_case（FormData 与排除字段保持原键名）。 */
export const convertTheKeysOfTheObjectToSnake = (data: unknown) => {
  if (isObject(data) && !isFormData(data)) {
    return Object.keys(data).reduce<Record<string, any>>((pre, cur) => {
      const value = (data as Record<string, any>)[cur];
      pre[isFormData(value) || isExcludedField(cur) ? cur : snakeCase(cur)] =
        value;
      return pre;
    }, {});
  }
  return data;
};

/** 从当前页面 URL 查询参数读取指定键的值。 */
export const getSearchValue = (key: string) => {
  const params = new URL(document.location as any).searchParams;
  return params.get(key);
};

/** 为数字字符串添加千位分隔符（逗号）。 */
export const formatNumberWithThousandsSeparator = (numberStr: string) => {
  const formattedNumber = numberStr.replace(/\B(?=(\d{3})+(?!\d))/g, ',');
  return formattedNumber;
};

const orderFactoryList = [
  LLMFactory.OpenAI,
  LLMFactory.Moonshot,
  LLMFactory.PPIO,
  LLMFactory.ZhipuAI,
  LLMFactory.Ollama,
  LLMFactory.Xinference,
  LLMFactory.Ai302,
  LLMFactory.CometAPI,
  LLMFactory.DeerAPI,
  LLMFactory.JiekouAI,
];

/** 按预设厂商顺序排列 LLM 工厂列表，未列出的项追加在末尾。 */
export const sortLLmFactoryListBySpecifiedOrder = (list: IFactory[]) => {
  const finalList: IFactory[] = [];
  orderFactoryList.forEach((orderItem) => {
    const index = list.findIndex((item) => item.name === orderItem);
    if (index !== -1) {
      finalList.push(list[index]);
    }
  });

  list.forEach((item) => {
    if (finalList.every((x) => x.name !== item.name)) {
      finalList.push(item);
    }
  });

  return finalList;
};

/** Select 组件 filterOption：按 label 子串不区分大小写匹配。 */
export const filterOptionsByInput = (
  input: string,
  option: { label: string; value: string } | undefined,
) => (option?.label ?? '').toLowerCase().includes(input.toLowerCase());

/** 数值保留 fixed 位小数，非数字原样返回。 */
export const toFixed = (value: unknown, fixed = 2) => {
  if (typeof value === 'number') {
    return value.toFixed(fixed);
  }
  return value;
};

/** 将 Python 风格 b'...' 字符串转为 Uint8Array（去掉首尾 b' 与 '）。 */
export const stringToUint8Array = (str: string) => {
  // const byteString = str.replace(/b'|'/g, '');
  const byteString = str.slice(2, -1);

  const uint8Array = new Uint8Array(byteString.length);
  for (let i = 0; i < byteString.length; i++) {
    uint8Array[i] = byteString.charCodeAt(i);
  }

  return uint8Array;
};

/** 十六进制字符串转 Uint8Array，格式非法时返回 undefined。 */
export const hexStringToUint8Array = (hex: string) => {
  const arr = hex.match(/[\da-f]{2}/gi);
  if (Array.isArray(arr)) {
    return new Uint8Array(
      arr.map(function (h) {
        return parseInt(h, 16);
      }),
    );
  }
};

/** 偶数长度十六进制字符串转 ArrayBuffer。 */
export function hexToArrayBuffer(input: string) {
  if (typeof input !== 'string') {
    throw new TypeError('Expected input to be a string');
  }

  if (input.length % 2 !== 0) {
    throw new RangeError('Expected string to be an even number of characters');
  }

  const view = new Uint8Array(input.length / 2);

  for (let i = 0; i < input.length; i += 2) {
    view[i / 2] = parseInt(input.substring(i, i + 2), 16);
  }

  return view.buffer;
}

/** 字节数格式化为人类可读单位（SI 1000 或二进制 1024）。 */
export function formatFileSize(bytes: number, si = true, dp = 1) {
  let nextBytes = bytes;
  const thresh = si ? 1000 : 1024;

  if (Math.abs(bytes) < thresh) {
    return nextBytes + ' B';
  }

  const units = si
    ? ['kB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB']
    : ['KiB', 'MiB', 'GiB', 'TiB', 'PiB', 'EiB', 'ZiB', 'YiB'];
  let u = -1;
  const r = 10 ** dp;

  do {
    nextBytes /= thresh;
    ++u;
  } while (
    Math.round(Math.abs(nextBytes) * r) / r >= thresh &&
    u < units.length - 1
  );

  return nextBytes.toFixed(dp) + ' ' + units[u];
}

/** 读取 documentElement 上 CSS 自定义属性的计算值。 */
function getCSSVariableValue(variableName: string): string {
  const computedStyle = getComputedStyle(document.documentElement);
  const value = computedStyle.getPropertyValue(variableName).trim();
  if (!value) {
    throw new Error(`CSS variable ${variableName} is not defined`);
  }
  return value;
}

/**
 * 解析颜色字符串为 RGB 三元组，支持 #hex、rgb()、var() 及 rgb(var()) 嵌套。
 * #fff -> [255, 255, 255]
 * var(--text-primary) -> 解析变量后同样返回 RGB
 */
 * #fff -> [255, 255, 255]
 * var(--text-primary) -> [var(--text-primary-r), var(--text-primary-g), var(--text-primary-b)]
 * */
/** 将多种 CSS 颜色表示统一解析为 [r, g, b] 整数元组。 */
export function parseColorToRGB(color: string): [number, number, number] {
  // Handling CSS variables (e.g. var(--accent-primary))
  let colorStr = color;
  if (colorStr.startsWith('var(')) {
    const varMatch = color.match(/var\(([^)]+)\)/);
    if (!varMatch) {
      console.error(`Invalid CSS variable: ${color}`);
      return [0, 0, 0];
    }
    const varName = varMatch[1];
    if (!varName) {
      console.error(`Invalid CSS variable: ${colorStr}`);
      return [0, 0, 0];
    }
    colorStr = getCSSVariableValue(varName);
  }

  // Handle rgb(var(--accent-primary)) format
  if (colorStr.startsWith('rgb(var(')) {
    const varMatch = colorStr.match(/rgb\(var\(([^)]+)\)\)/);
    if (!varMatch) {
      console.error(`Invalid nested CSS variable: ${color}`);
      return [0, 0, 0];
    }
    const varName = varMatch[1];
    if (!varName) {
      console.error(`Invalid nested CSS variable: ${colorStr}`);
      return [0, 0, 0];
    }
    // Get the CSS variable value which should be in format "r, g, b"
    const rgbValues = getCSSVariableValue(varName);
    const rgbMatch = rgbValues.match(/^(\d+),?\s*(\d+),?\s*(\d+)$/);
    if (rgbMatch) {
      return [
        parseInt(rgbMatch[1]),
        parseInt(rgbMatch[2]),
        parseInt(rgbMatch[3]),
      ];
    }
    console.error(`Unsupported RGB CSS variable format: ${rgbValues}`);
    return [0, 0, 0];
  }

  // Handles hexadecimal colors (e.g. #FF5733)
  if (colorStr.startsWith('#')) {
    const cleanedHex = colorStr.replace(/^#/, '');
    if (cleanedHex.length === 3) {
      return [
        parseInt(cleanedHex[0] + cleanedHex[0], 16),
        parseInt(cleanedHex[1] + cleanedHex[1], 16),
        parseInt(cleanedHex[2] + cleanedHex[2], 16),
      ];
    }
    return [
      parseInt(cleanedHex.slice(0, 2), 16),
      parseInt(cleanedHex.slice(2, 4), 16),
      parseInt(cleanedHex.slice(4, 6), 16),
    ];
  }

  // Handling RGB colors (e.g., rgb(255, 87, 51))
  if (colorStr.startsWith('rgb')) {
    const rgbMatch = colorStr.match(/rgb\((\d+),\s*(\d+),\s*(\d+)\)/);
    if (rgbMatch) {
      return [
        parseInt(rgbMatch[1]),
        parseInt(rgbMatch[2]),
        parseInt(rgbMatch[3]),
      ];
    }
    console.error(`Unsupported RGB format: ${colorStr}`);
    return [0, 0, 0];
  }
  console.error(`Unsupported colorStr format: ${colorStr}`);
  return [0, 0, 0];
}

/**
 *
 * @param color eg: #fff, or var(--color-text-primary)
 * @param opcity 0~1
 * @return rgba(r,g,b,opcity)
 */
/** 解析颜色并输出 rgba(r,g,b,opacity) 字符串。 */
export function parseColorToRGBA(color: string, opcity = 1): string {
  const [r, g, b] = parseColorToRGB(color);
  return `rgba(${r},${g},${b},${opcity})`;
}

/** 长字符串中间省略：保留首尾各 front/back 字符，中间用 … 连接。 */
export function middleEllipsis(str: string, front = 12, back = 8) {
  if (str.length <= front + back) return str;
  return `${str.slice(0, front)}…${str.slice(-back)}`;
}
