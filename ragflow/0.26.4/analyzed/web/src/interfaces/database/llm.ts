// llm.ts — 第三方 LLM 模型、厂商、实例与默认模型配置类型。

/** 第三方/OpenAI 兼容模型条目：可用性、token 上限与 model_type。 */
export interface IThirdOAIModel {
  available: boolean;
  create_date: string;
  create_time: number;
  fid: string;
  id: number;
  llm_name: string;
  max_tokens: number;
  model_type: string;
  status: string;
  tags: string;
  update_date: string;
  update_time: number;
  tenant_id?: string;
  tenant_name?: string;
  is_tools: boolean;
}

/** 按厂商/分组键索引的第三方模型集合。 */
export type IThirdOAIModelCollection = Record<string, IThirdOAIModel[]>;

/** LLM 厂商/工厂元信息：logo、名称与标签。 */
export interface IFactory {
  create_date: string;
  create_time: number;
  logo: string;
  name: string;
  status: string;
  tags: string;
  update_date: string;
  update_time: number;
}

/** 租户已添加模型分组：模型列表与 tags 字符串。 */
export interface IMyLlmValue {
  llm: Llm[];
  tags: string;
}

/** 单条已添加模型：名称、类型、状态与已用 token。 */
export interface Llm {
  name: string;
  type: string;
  status: '0' | '1';
  used_token: number;
}

/** 可接入的模型提供商：支持的 model_types 与默认 URL 模板。 */
export interface IAvailableProvider {
  name: string;
  model_types: string[];
  url: { default?: string; [key: string]: string | undefined };
}

/** 用户配置的提供商实例：API Key、区域与可选 base_url。 */
export interface IProviderInstance {
  api_key: string;
  id: string;
  instance_name: string;
  provider_id: string;
  region: string;
  status: string;
  /** 仅 showProviderInstance 返回，用于 ProviderModal 预填 base_url。 */
  base_url?: string;
}
/** 已添加模型摘要：关联 provider/instance 与 model_type 列表。 */
export interface IAddedModel {
  model_type: string[];
  name: string;
  provider_id: string;
  provider_name: string;
  instance_id: string;
  instance_name: string;
}

/** 某实例下的具体模型：max_tokens、类型与启用状态。 */
export interface IInstanceModel {
  max_tokens: number;
  model_type: string[];
  name: string;
  status: string;
}

/** 租户某 model_type 的默认模型绑定（chat/embedding 等）。 */
export interface IDefaultModel {
  enable: boolean;
  model_instance: string;
  model_name: string;
  model_provider: string;
  model_type: string;
}
