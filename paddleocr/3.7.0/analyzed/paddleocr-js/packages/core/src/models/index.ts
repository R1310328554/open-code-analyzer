/*
 * Copyright (c) 2026 PaddlePaddle Authors. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

// 模型子模块 barrel：统一导出 det/rec 工厂、默认配置与解析函数
export {
  DEFAULT_DET_MODEL_PARSE_FALLBACKS,
  DEFAULT_DET_MODEL_CONFIG,
  createDetModel,
  createDetModelSession,
  parseDetModelConfigText
} from "./det";
export {
  DEFAULT_REC_MODEL_PARSE_FALLBACKS,
  DEFAULT_REC_RUNTIME_LIMITS,
  DEFAULT_REC_MODEL_CONFIG,
  createRecModel,
  createRecModelSession,
  parseRecModelConfigText
} from "./rec";
// 识别运行时 batchSize 覆盖类型
export type { RecRuntimeOverrides } from "./rec";
