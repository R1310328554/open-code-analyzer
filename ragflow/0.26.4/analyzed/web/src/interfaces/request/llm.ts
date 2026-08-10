// llm.ts — 添加/删除 LLM、Provider 实例与默认模型等 API 请求体。

/** 添加 LLM/Provider 连接：工厂名、base_url、api_key 与 model_info 列表。 */
export interface IAddLlmRequestBody {
  llm_factory: string; // 厂商标识，如 Ollama
  // model_name: string;
  // model_type: string | string[];
  base_url?: string; // 服务端点，按 model_type 区分 chat/embedding 等
  api_key?: string | Record<string, any>;
  max_tokens: number;
  is_tools?: boolean;
  region?: string;
  model_info: IModelInfo[];
}

/** 单条模型描述：名称、类型、max_tokens 与可选 extra（如 is_tools）。 */
export interface IModelInfo {
  model_name: string;
  model_type: string | string[];
  max_tokens: number;
  /** 单模型扩展字段（如 features 推导的 is_tools），兼容旧版单模型 payload。 */
  extra?: Record<string, any>;
}

/** 删除已添加 LLM：llm_factory 与可选 llm_name。 */
export interface IDeleteLlmRequestBody {
  llm_factory: string; // Ollama
  llm_name?: string;
}

/** 列出 Provider：可选仅返回 available 提供商。 */
export interface IListProvidersRequestParams {
  available?: boolean;
}

/** 添加 Provider 类型：provider_name。 */
export interface IAddProviderRequestBody {
  provider_name: string;
}

/** 添加 Provider 实例：继承 IAddLlmRequestBody 并含 instance_name。 */
export type IAddProviderInstanceRequestBody = IAddLlmRequestBody & {
  instance_name: string;
  region?: string;
};

/** 批量删除 Provider 实例：provider_name 与 instances 名称列表。 */
export interface IDeleteProviderInstanceRequestBody {
  provider_name: string;
  instances: string[];
}

/** 查看单实例详情：provider_name 与 instance_name。 */
export interface IShowProviderInstanceRequestParams {
  provider_name: string;
  instance_name: string;
}

/** 向实例添加模型：model_name、model_type 数组与 max_tokens。 */
export interface IAddInstanceModelRequestBody {
  model_name: string;
  model_type: string[];
  max_tokens: number;
  extra?: Record<string, any>;
}

/** 编辑实例下模型：model_name 与 model_type 数组。 */
export interface IEditInstanceModelRequestBody {
  model_name: string[];
  model_type: string[];
}

/** 列出全部模型：可选按 type 过滤。 */
export interface IListAllModelsRequestParams {
  type?: string;
}

/** 启用/停用实例内模型：provider、instance、model_name 与 status。 */
export interface IUpdateModelStatusRequestBody {
  provider_name: string;
  instance_name: string;
  model_name: string;
  status: 'active' | 'inactive';
}

/** 设置租户默认模型：provider、instance、model_type 与 model_name。 */
export interface ISetDefaultModelRequestBody {
  model_provider: string;
  model_instance: string;
  model_type: string;
  model_name: string;
}

/** list-provider-models 接口返回的单条可用模型描述。 */
/** Provider 可用模型：name、max_tokens、model_types 与 features。 */
export interface IProviderModelItem {
  name: string;
  max_tokens: number;
  model_types: string[];
  features: string[] | null;
}

/** 拉取 Provider 可用模型列表：与 verifyProviderConnection 表单字段一致。 */
/** list-provider-models 请求体：provider_name、凭证与可选 model_info。 */
export interface IListProviderModelsRequestBody {
  provider_name: string;
  api_key: string;
  base_url?: string;
  region?: string;
  model_info?: IModelInfo[];
}
