/*
 * Copyright (c) 2026 PaddlePaddle Authors. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

// 资源子模块 barrel：模型资产类型、默认 URL 映射与 tar 加载 API
export type { ModelAsset, ModelAssetsMap, ModelLoadResult, ModelLoadSummary } from "./model-asset";
export {
  DEFAULT_MODEL_ASSETS,
  MODEL_ENTRY_PATHS,
  assertModelResourceSlot,
  assertModelResources,
  getModelEntryPath,
  loadModelAsset,
  normalizeAssets,
  normalizeModelAsset
} from "./model-asset";
// tar 解压工具重导出，供 loadModelAsset 与外部测试使用
export { extractTarEntries, pickTarEntry } from "./tar";
