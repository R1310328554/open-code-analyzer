/*
 * Copyright (c) 2026 PaddlePaddle Authors. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

// OpenCV.js WASM 运行时初始化：单例缓存 Promise，兼容 ESM 动态导入与 onRuntimeInitialized
import type { OpenCv } from "@techstark/opencv-js";
import cvModule from "@techstark/opencv-js";

// 全局缓存 initOpenCvRuntime 结果，避免重复加载 WASM
let cachedCvPromise: Promise<{ cv: OpenCv }> | null = null;

  // 解析 cv 模块：await Promise 或等待 onRuntimeInitialized 回调
async function getOpenCv(): Promise<{ cv: OpenCv }> {
  let cv: OpenCv;
  if (cvModule instanceof Promise) {
    cv = await cvModule;
  } else {
    const mod = cvModule as { Mat?: unknown; onRuntimeInitialized?: () => void };
    if (mod.Mat) {
      cv = cvModule as OpenCv;
    } else {
      await new Promise<void>((resolve) => {
        mod.onRuntimeInitialized = () => {
          resolve();
        };
      });
      cv = cvModule as OpenCv;
    }
  }
  return { cv };
}

  // 对外入口：首次调用触发加载，失败时清空缓存以便重试
export async function initOpenCvRuntime(): Promise<{ cv: OpenCv }> {
  if (!cachedCvPromise) {
    cachedCvPromise = getOpenCv().catch((error: unknown) => {
      cachedCvPromise = null;
      throw error;
    });
  }
  return cachedCvPromise;
}
