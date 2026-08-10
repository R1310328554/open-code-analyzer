// payload-utils.ts — 将扁平提供商表单 payload 拆分为实例级与模型级请求体。

import {
  IAddInstanceModelRequestBody,
  IAddProviderInstanceRequestBody,
} from '@/interfaces/request/llm';

/** 模型级保留字段（直接映射到 model payload）。 */
const MODEL_RESERVED_KEYS = new Set([
  'llm_name',
  'model_name',
  'model_type',
  'max_tokens',
]);

/** 实例级保留字段（不参与 api_key 对象合并）。 */
const INSTANCE_RESERVED_KEYS = new Set([
  'instance_name',
  'llm_factory',
  'provider_name',
  'api_base',
  'base_url',
  'region',
  'verify',
  'model_info',
]);

/** 写入 model extra 的可选扩展键（is_tools、vision、Somark 等）。 */
export const MODEL_EXTRA_KEYS = new Set([
  'is_tools',
  'vision',
  'provider_order',
  'api_version',
  'somark_image_format',
  'somark_formula_format',
  'somark_table_format',
  'somark_cs_format',
  'somark_enable_text_cross_page',
  'somark_enable_table_cross_page',
  'somark_enable_title_level_recognition',
  'somark_enable_inline_image',
  'somark_enable_table_image',
  'somark_enable_image_understanding',
  'somark_keep_header_footer',
]);

export const MODEL_FIELD_NAMES = new Set<string>([
  ...MODEL_RESERVED_KEYS,
  ...MODEL_EXTRA_KEYS,
]);

/** 判断字段名是否属于模型相关字段集合。 */
export const isModelField = (fieldName: string) =>
  MODEL_FIELD_NAMES.has(fieldName);

type FlatPayload = Record<string, any>;

export type SplitResult = {
  instancePayload: Omit<
    IAddProviderInstanceRequestBody,
    'llm_name' | 'model_type' | 'max_tokens'
  > & {
    base_url?: string;
    region?: string;
  };
  modelPayload: IAddInstanceModelRequestBody;
};

/** 收集非保留键并合并 api_key（对象或字符串）为实例 api_key 字段。 */
const collectApiKeyExtras = (payload: FlatPayload) => {
  const extras: Record<string, any> = {};
  let apiKeyValue: any = undefined;
  for (const [key, value] of Object.entries(payload)) {
    if (value === undefined) continue;
    if (key === 'api_key') {
      apiKeyValue = value;
      continue;
    }
    if (INSTANCE_RESERVED_KEYS.has(key)) continue;
    if (MODEL_RESERVED_KEYS.has(key)) continue;
    if (MODEL_EXTRA_KEYS.has(key)) continue;
    extras[key] = value;
  }
  if (apiKeyValue && typeof apiKeyValue === 'object') {
    return { ...apiKeyValue, ...extras };
  }
  if (Object.keys(extras).length === 0) {
    return apiKeyValue ?? '';
  }
  if (apiKeyValue !== undefined && apiKeyValue !== '') {
    return { api_key: apiKeyValue, ...extras };
  }
  return extras;
};

/** 从 payload 提取 MODEL_EXTRA_KEYS 中非空值作为 model extra。 */
const collectModelExtras = (payload: FlatPayload) => {
  const extras: Record<string, any> = {};
  for (const key of MODEL_EXTRA_KEYS) {
    if (payload[key] !== undefined && payload[key] !== '') {
      extras[key] = payload[key];
    }
  }
  return extras;
};

/**
 * 将 ProviderModal 提交的扁平对象拆成 instancePayload 与 modelPayload，
 * 供 addProviderInstance / addInstanceModel 分别调用。
 */
export const splitProviderPayload = (payload: FlatPayload): SplitResult => {
  const {
    instance_name,
    llm_factory,
    base_url,
    api_base,
    region,
    model_info,
    ...other
  } = payload;
  const instancePayload = {
    instance_name: instance_name as string,
    llm_factory: llm_factory as string,
    api_key: collectApiKeyExtras(payload),
    base_url: (base_url ?? api_base) as string | undefined,
    region: (region as string | undefined) || 'default',
    model_info: model_info,
    ...other,
  };

  const modelExtra = collectModelExtras(payload);

  const modelPayload = {
    model_name: (payload.model_name ?? payload.llm_name) as string,
    model_type: payload.model_type,
    max_tokens: payload.max_tokens as number,
    ...(Object.keys(modelExtra).length > 0 ? { extra: modelExtra } : {}),
  };

  return {
    instancePayload: instancePayload as SplitResult['instancePayload'],
    modelPayload,
  };
};
