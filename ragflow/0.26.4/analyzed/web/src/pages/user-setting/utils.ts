/**
 * user-setting/utils.ts — 用户设置页工具函数：判断 LLM 工厂是否为本地部署类型。
 */

import { LocalLlmFactories } from './constants';

/** 判断给定 llmFactory 是否属于 LocalLlmFactories 本地模型工厂列表。 */
export const isLocalLlmFactory = (llmFactory: string) =>
  LocalLlmFactories.some((x) => x === llmFactory);
