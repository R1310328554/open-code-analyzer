/*
 * Copyright (c) 2026 PaddlePaddle Authors. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

// @paddleocr-js/core 公共类型 barrel：聚合 OpenCV、模型、pipeline 与运行时类型
export type { OpenCv, Mat, MatVector, Size, Rect, Scalar, RotatedRect } from "@techstark/opencv-js";

// 几何与检测通用类型重导出
export type { Point2D, NormalizeConfig, DetBox, MiniBox } from "../models/common";

// 文本检测模型配置、后处理与运行时覆盖类型
export type {
  DetModelConfig,
  DetPostprocessConfig,
  DetModel,
  DetResult,
  DetRuntimeOverrides,
  LimitType
} from "../models/det";

// 文本识别模型相关类型
export type { RecModelConfig, RecModel, RecResult, RecRuntimeOverrides } from "../models/rec";

// OCR predict 运行时参数与解析结果类型
export type {
  OcrRuntimeParamsInput,
  OcrModelConfig,
  ResolvedOcrParams
} from "../pipelines/ocr/runtime-params";

// OCR 流水线输出：items、metrics 与初始化摘要
export type {
  OcrResult,
  OcrResultItem,
  OcrResultMetrics,
  OcrResultRuntime,
  InitializationSummary,
  OcrPipelineRunnerOptions,
  SourceToMatFn
} from "../pipelines/ocr/core";

// pipeline YAML 规范化后的配置与模型选择
export type {
  NormalizedPipelineConfig,
  PipelineModelSelection,
  PipelineRuntimeDefaults
} from "../pipelines/ocr/config";

// create() 选项解析：backend、ORT 与 worker 配置
export type {
  ResolvedBackend,
  ResolvedOcrOptions,
  NormalizedOrtOptions,
  WorkerResolvedOptions
} from "../pipelines/ocr/shared";

// PaddleOCR 工厂类 create 选项
export type { PaddleOCRCreateOptions } from "../pipelines/ocr/index";

// 模型 tar 资产描述符类型
export type { ModelAsset, ModelAssetsMap } from "../resources/model-asset";

// ORT 运行时模块与 session 状态类型
export type {
  OrtModule,
  WebGpuState,
  OrtOptions,
  OrtRuntimeResult,
  SessionState
} from "../runtime/ort";

// 浏览器图像源与 worker 载荷类型
export type {
  ImageSource,
  SourceMatResult,
  WorkerPayload,
  WorkerPayloadResult
} from "../platform/browser";

// worker 主从通信协议消息类型
export type {
  TransportRequest,
  TransportResponse,
  TransportSuccessResponse,
  TransportErrorResponse,
  SerializedError
} from "../worker/protocol";

export type { WorkerOptions } from "../worker/client";
export type { MessageHandler } from "../worker/entry";
