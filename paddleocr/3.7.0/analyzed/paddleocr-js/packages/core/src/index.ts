/*
 * Copyright (c) 2026 PaddlePaddle Authors. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

// @paddleocr/paddleocr-js 公共 API 重导出：Pipeline、模型类型与运行时选项
export {
  PaddleOCR,
  normalizeOcrPipelineConfig,
  parseOcrPipelineConfigText
} from "./pipelines/ocr/index";

// 检测/识别共用几何与归一化类型
export type { Point2D, NormalizeConfig, DetBox } from "./models/common";

// 文本检测模型配置、运行时覆盖与推理结果类型
export type {
  DetModelConfig,export type {
  DetModelConfig,
  DetPostprocessConfig,
  DetModel,
  DetResult,
  DetRuntimeOverrides,
  LimitType
} from "./models/det";

// 文本识别模型与 CTC 解码结果类型
export type { RecModelConfig, RecModel, RecResult, RecRuntimeOverrides } from "./models/rec";

export type {
  OcrRuntimeParamsInput,
  OcrModelConfig,
  ResolvedOcrParams
} from "./pipelines/ocr/runtime-params";

export type {
  OcrResult,
  OcrResultItem,
  OcrResultMetrics,
  OcrResultRuntime,
  InitializationSummary,
  OcrPipelineRunnerOptions
} from "./pipelines/ocr/core";

export type {
  NormalizedPipelineConfig,
  PipelineModelSelection,
  PipelineRuntimeDefaults
} from "./pipelines/ocr/config";

export type {
  ResolvedBackend,
  ResolvedOcrOptions,
  NormalizedOrtOptions,
  WorkerResolvedOptions
} from "./pipelines/ocr/shared";

export type { ModelAsset, ModelAssetsMap } from "./resources/model-asset";

// ONNX Runtime Web 后端与 WebGPU 状态类型
export type { WebGpuState, OrtOptions } from "./runtime/ort";

export type { ImageSource, SourceMatResult } from "./platform/browser";

export type { PaddleOCRCreateOptions } from "./pipelines/ocr/index";
