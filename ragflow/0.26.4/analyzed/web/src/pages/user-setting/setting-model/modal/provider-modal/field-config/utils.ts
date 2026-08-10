// field-config/utils.ts — 提供商表单：字符串工具与 model_info 组装。

import { IModelInfo } from '@/interfaces/request/llm';

/** 将字符串首字母大写（用于 model_type 选项展示）。 */
export function capitalize(s: string): string {
  return s.charAt(0).toUpperCase() + s.slice(1);
}

/** 当 model_type 含 chat 且 vision 开启时，自动追加 image2text 能力。 */
export function applyChatToImage2Text(
  modelType: string[] | string | undefined,
  vision?: boolean,
): string[] {
  const arr = Array.isArray(modelType)
    ? modelType
    : modelType
      ? [modelType]
      : [];
  if (arr.includes('chat') && vision) {
    return [...arr, 'image2text'];
  }
  return arr;
}

/**
 * 从表单值组装 verify/submit 所需的 IModelInfo[]。
 *
 * 优先级：
 * 1. `values.model_info` 非空数组（列表选择器已合并）则原样返回；
 * 2. 否则由 model_name / model_type / max_tokens 等单字段拼一条记录，
 *    is_tools / vision 写入 extra.is_tools；
 * 3. 无 model_name 时返回空数组，由调用方决定是否短路。
 */
export const buildModelInfoFromValues = (
  values: Record<string, any>,
): IModelInfo[] => {
  if (Array.isArray(values.model_info) && values.model_info.length > 0) {
    return values.model_info;
  }
  if (!values.model_name) return [];
  const is_tools = values.is_tools ?? values.vision;
  const entry: IModelInfo = {
    model_name: values.model_name,
    model_type: values.model_type ?? [],
    max_tokens: values.max_tokens ?? 0,
  };
  if (is_tools !== undefined) {
    entry.extra = { is_tools };
  }
  return [entry];
};
