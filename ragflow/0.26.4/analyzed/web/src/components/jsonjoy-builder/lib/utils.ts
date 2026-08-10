// utils.ts — jsonjoy-builder 通用工具：Tailwind 类名合并与 schema 类型展示标签。

import { clsx, type ClassValue } from 'clsx';
import { twMerge } from 'tailwind-merge';
import type { Translation } from '../i18n/translation-keys.ts';
import type { SchemaType } from '../types/json-schema';

/** 合并 clsx 与 tailwind-merge，避免冲突的 Tailwind 类名。 */
export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

// 向后兼容：schema 类型对应的 Tailwind 颜色类
/** 按 JSON Schema 类型返回 Tailwind 文本/背景色类名。 */
export const getTypeColor = (type: SchemaType): string => {
  switch (type) {
    case 'string':
      return 'text-blue-500 bg-blue-50';
    case 'number':
    case 'integer':
      return 'text-purple-500 bg-purple-50';
    case 'boolean':
      return 'text-green-500 bg-green-50';
    case 'object':
      return 'text-orange-500 bg-orange-50';
    case 'array':
      return 'text-pink-500 bg-pink-50';
    case 'null':
      return 'text-gray-500 bg-gray-50';
  }
};

// 按当前语言包返回 schema 类型的可读标签
/** 使用 Translation 文案将 SchemaType 映射为本地化显示名。 */
export const getTypeLabel = (t: Translation, type: SchemaType): string => {
  switch (type) {
    case 'string':
      return t.schemaTypeString;
    case 'number':
    case 'integer':
      return t.schemaTypeNumber;
    case 'boolean':
      return t.schemaTypeBoolean;
    case 'object':
      return t.schemaTypeObject;
    case 'array':
      return t.schemaTypeArray;
    case 'null':
      return t.schemaTypeNull;
  }
};
