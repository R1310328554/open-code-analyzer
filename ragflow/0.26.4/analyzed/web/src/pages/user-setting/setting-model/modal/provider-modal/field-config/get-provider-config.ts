// get-provider-config.ts — 按 llmFactory 解析 ProviderConfig（映射表 → 本地 LLM → 通用 ApiKey）。

import type { ProviderConfig } from '../types';
import { GenericApiKeyConfig } from './generic-api-key-config';
import { LocalLlmConfigs } from './local-llm-configs';
import { ProviderConfigMap } from './provider-config-map';

/**
 * 获取指定工厂的 ProviderConfig。
 * 查找顺序：ProviderConfigMap → LocalLlmConfigs → GenericApiKeyConfig。
 */
export function getProviderConfig(llmFactory: string): ProviderConfig {
  // 特殊工厂（ModalMap 中约 11 个）已在 ProviderConfigMap / LocalLlmConfigs 中配置
  // Among which AzureOpenAI/VolcEngine/GoogleCloud/TencentCloud/XunFeiSpark/BaiduYiYan/FishAudio are in ProviderConfigMap
  // Bedrock/MinerU/PaddleOCR/OpenDataLoader are out of the merge scope and use the original modal

  if (ProviderConfigMap[llmFactory]) {
    return ProviderConfigMap[llmFactory];
  }

  if (LocalLlmConfigs[llmFactory]) {
    return LocalLlmConfigs[llmFactory];
  }

  // 回退：克隆通用 ApiKey 配置并写入当前 llmFactory
  return {
    ...GenericApiKeyConfig,
    llmFactory,
  };
}
