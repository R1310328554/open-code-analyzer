/**
 * user-setting/interface.ts — 用户设置相关 API 请求体类型。
 */

/** 提交 LLM API Key 时的字段：实例名、密钥与 base_url。 */
export interface ApiKeyPostBody {
  instance_name: string;
  api_key: string;
  base_url: string;
  group_id?: string;
}
