import { getCachedLlmList } from './llm-cache';

/** 从接口返回的复合模型名（如 deepseek-r1___OpenAI-API）提取真实模型名（双下划线前段）。 */
export function getRealModelName(llmName: string) {
  return llmName.split('__').at(0) ?? '';
}

/** 在缓存的 LLM 列表中按 modelName + factoryId 查找对应 tenant 模型 id。 */
export function getTenantModelId(
  llmList: Record<string, any>,
  modelName: string,
  factoryId: string,
): string {
  // Iterate through all providers in the LLM list
  for (const [provider, data] of Object.entries(llmList)) {
    if (data.llm && Array.isArray(data.llm)) {
      // Handle /v1/llm/my_llms format
      const model = data.llm.find(
        (m: any) => m.name === modelName && provider === factoryId,
      );
      if (model && model.id) {
        return model.id;
      }
    } else if (Array.isArray(data)) {
      // Handle /v1/llm/list format
      const model = data.find(
        (m: any) => m.llm_name === modelName && m.fid === factoryId,
      );
      if (model && model.id) {
        return model.id;
      }
    }
  }
  return '';
}

/** 拼接 modelName@instanceName@providerName 复合值。 */
export function buildModelValue(model: {
  model_name: string;
  model_instance: string;
  model_provider: string;
}) {
  return `${model.model_name}@${model.model_instance}@${model.model_provider}`;
}

/** 解析 modelName@instanceName@providerName，格式非法时返回 null。 */
export function parseModelValue(val: string) {
  if (!val) return null;
  const firstAt = val.indexOf('@');
  const lastAt = val.lastIndexOf('@');
  if (firstAt === -1 || firstAt === lastAt) return null;
  return {
    model_name: val.substring(0, firstAt),
    model_instance: val.substring(firstAt + 1, lastAt),
    model_provider: val.substring(lastAt + 1),
  };
}

/** 从 model UUID（支持 #instance 后缀）解析 modelName 与 factoryId。 */
export function parseModelUuid(uuid: string): {
  modelName: string;
  factoryId: string;
} {
  const hashIndex = uuid.indexOf('#');
  const core = hashIndex === -1 ? uuid : uuid.slice(0, hashIndex);
  const [modelName, factoryId] = core.split('@');
  return { modelName, factoryId };
}

/** 请求体模型字段名到 tenant_* 字段名的映射表。 */
type ModelParamMap = {
  [key: string]: string;
};

/** llm_id/embd_id 等字段 → tenant_llm_id/tenant_embd_id 等。 */
const modelParamMap: ModelParamMap = {
  llm_id: 'tenant_llm_id',
  embd_id: 'tenant_embd_id',
  asr_id: 'tenant_asr_id',
  tts_id: 'tenant_tts_id',
  img2txt_id: 'tenant_img2txt_id',
  rerank_id: 'tenant_rerank_id',
};

/** 仅对白名单 URL 自动注入 tenant 模型参数。 */
const API_WHITELIST = [
  '/api/v1/users/me/models',
  '/api/v1/chats',
  '/v1/canvas/set',
  '/v1/canvas/setting',
  '/api/v1/searches/',
  '/api/v1/memories',
  '/api/v1/datasets',
  '/v1/dataflow/set',
];

/** 判断请求 URL 是否命中 tenant 参数注入白名单。 */
export function isUrlInWhitelist(url: string): boolean {
  return API_WHITELIST.some((endpoint) => url.includes(endpoint));
}

/** 递归遍历请求体，将 llm_id 等字段解析为对应 tenant_* id（依赖 LLM 缓存）。 */
export function addTenantParams(data: any, url?: string): any {
  if (!data || typeof data !== 'object') return data;

  // If URL is provided and not in whitelist, return original data
  if (url && !isUrlInWhitelist(url)) {
    return data;
  }

  const llmList = getCachedLlmList();
  if (!llmList) return data;

  // Handle arrays
  if (Array.isArray(data)) {
    return data.map((item) => addTenantParams(item, url));
  }

  const newData = { ...data };

  // Iterate through model parameters and add corresponding tenant parameters
  for (const [paramName, tenantParamName] of Object.entries(modelParamMap)) {
    if (newData[paramName]) {
      try {
        const { modelName, factoryId } = parseModelUuid(newData[paramName]);
        const tenantModelId = getTenantModelId(llmList, modelName, factoryId);
        if (tenantModelId) {
          newData[tenantParamName] = tenantModelId;
        }
      } catch (error) {
        console.error(`Error processing ${paramName}:`, error);
      }
    }
  }

  // Recursively process nested objects
  for (const [key, value] of Object.entries(newData)) {
    if (value && typeof value === 'object' && !modelParamMap[key]) {
      newData[key] = addTenantParams(value, url);
    }
  }

  return newData;
}
