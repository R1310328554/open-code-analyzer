/*
 * Copyright (c) 2026 PaddlePaddle Authors. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

// OCR Web Worker 入口：注册 init/predict/dispose 消息处理器
import { attachWorkerMessageHandler } from "../../worker/entry";
import { sourcePayloadToMat, ensureServedFromHttp } from "../../platform/worker";
import type { OcrPipelineRunnerOptions } from "./core";
import { OcrPipelineRunner } from "./core";
import type { OcrRuntimeParamsInput } from "./runtime-params";

  // 闭包持有 OcrPipelineRunner 单例，复用 worker 侧 OpenCV/ORT 运行时
function createPaddleOCRWorkerMessageHandler() {
  let ocr: OcrPipelineRunner | null = null;

    // 重建 runner、注入 worker 专用 sourcePayloadToMat 与 HTTP 校验
  async function handleInit(payload: Record<string, unknown>) {
    await ocr?.dispose();
    ocr = new OcrPipelineRunner({
      ...(payload.options as OcrPipelineRunnerOptions),
      ensureServedFromHttp,
      sourceToMat: sourcePayloadToMat
    });
    const summary = await ocr.initialize();
    return {
      summary,
      modelConfig: ocr.getModelConfig()
    };
  }

    // 将主线程传来的 sources 数组委托 runner.predict
  async function handlePredict(payload: Record<string, unknown>) {
    if (!ocr) {
      throw new Error("OCR worker is not initialized.");
    }
    const sources = payload.sources;
    return ocr.predict(sources, (payload.params || {}) as OcrRuntimeParamsInput);
  }

  async function handleDispose() {
    await ocr?.dispose();
    ocr = null;
    return {};
  }

  return async function handleMessage(type: string, payload: Record<string, unknown>) {
    switch (type) {
      case "init":
        return handleInit(payload);
      case "predict":
        return handlePredict(payload);
      case "dispose":
        return handleDispose();
      default:
        throw new Error(`Unsupported worker request type "${type}".`);
    }
  };
}

// 模块加载时立即挂载 worker 消息循环
attachWorkerMessageHandler(createPaddleOCRWorkerMessageHandler());
