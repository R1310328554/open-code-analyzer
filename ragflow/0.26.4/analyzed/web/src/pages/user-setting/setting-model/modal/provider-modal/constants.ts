// constants.ts — 提供商弹窗：启用「列出模型」选择器的工厂集合。

import { LLMFactory } from '@/constants/llm';

/**
 * 启用「列出模型」选择器 UI 的 LLM 工厂集合。
 *
 * 这些工厂在弹窗中隐藏 model_name / model_type / max_tokens / is_tools
 * 等传统字段，改为展示「列出模型」按钮，从 `/providers/<factory>/models`
 * 拉取可用模型并支持多选；选中项转为 `IModelInfo` 后以 `model_info` 提交。
 * 未列入的工厂仍直接渲染上述四个模型相关字段。
 */
export const LIST_MODEL_PROVIDERS = new Set<string>([
  LLMFactory.Ollama,
  LLMFactory.OpenRouter,
  LLMFactory.VLLM,
  LLMFactory.OpenAiAPICompatible,
  LLMFactory.LMStudio,
  LLMFactory.VolcEngine,
  LLMFactory.Xinference,
  LLMFactory.LocalAI,
  LLMFactory.BaiduYiYan,
  LLMFactory.NewAPI,

  // LLMFactory.HuggingFace,
  // LLMFactory.GoogleCloud,
  // LLMFactory.TencentCloud,
  // LLMFactory.XunFeiSpark,
  // LLMFactory.GPUStack,
  // LLMFactory.FishAudio,
  // LLMFactory.MinerU,
  // LLMFactory.PaddleOCR,
]);

/**
 * 由「列出模型」选择器接管的表单字段名（启用选择器时不注册到动态表单）。
 *
 * 同时作为 viewMode 下仍可编辑字段的白名单——viewMode 中其余字段禁用，
 * 仅允许修改模型相关项。
 */
export const LIST_MODEL_FIELD_NAMES = new Set<string>([
  'model_name',
  'model_type',
  'max_tokens',
  'is_tools',
]);
