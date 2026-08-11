/*
 * Copyright (c) 2026 PaddlePaddle Authors. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

// ONNX Runtime Web 运行时封装：WebGPU/WASM 探测、环境配置与 InferenceSession 创建
export type OrtModule = typeof import("onnxruntime-web");

  // WebGPU 可用性探测结果：available 标志与不可用时的原因说明
export interface WebGpuState {
  available: boolean;
  reason: string;
}

  // ORT 初始化选项：backend 选择、wasm 路径、线程数与 proxy 开关
export interface OrtOptions {
  backend?: "webgpu" | "wasm" | "auto" | (string & {});
  wasmPaths?: string;
  numThreads?: number;
  simd?: boolean;
  proxy?: boolean;
  disableWasmProxy?: boolean;
}

  // initOrtRuntime 返回值：ort 模块实例、WebGPU 状态与解析后的 backend
export interface OrtRuntimeResult {
  ort: OrtModule;
  webgpuState: WebGpuState;
  backend: string;
}

  // 成功创建的推理会话及其 execution provider 名称
export interface SessionState {
  session: import("onnxruntime-web").InferenceSession;
  provider: string;
}

let ortModulePromise: Promise<OrtModule> | null = null;

  // 动态 import onnxruntime-web，单例缓存模块 Promise
async function loadOrtModule(): Promise<OrtModule> {
  if (ortModulePromise) {
    return ortModulePromise;
  }
  ortModulePromise = import("onnxruntime-web");
  return ortModulePromise;
}

interface GpuLike {
  requestAdapter(): Promise<unknown>;
}

  // 通过 navigator.gpu.requestAdapter 检测浏览器 WebGPU 支持
export async function detectWebGpuAvailability(): Promise<WebGpuState> {
  const gpu = (globalThis.navigator as (Navigator & { gpu?: GpuLike }) | undefined)?.gpu;
  if (!gpu?.requestAdapter) {
    return {
      available: false,
      reason: "navigator.gpu is unavailable in this browser."
    };
  }
  try {
    const adapter = await gpu.requestAdapter();
    if (!adapter) {
      return {
        available: false,
        reason: "The browser did not return a WebGPU adapter."
      };
    }
    return {
      available: true,
      reason: ""
    };
  } catch (err: unknown) {
    return {
      available: false,
      reason: err instanceof Error ? err.message : "Failed to request a WebGPU adapter."
    };
  }
}

  // 按 backend（webgpu/wasm/auto）生成 executionProviders 候选列表
export function getProviderCandidates(backend: string, webgpuState: WebGpuState): string[][] {
  if (backend === "webgpu") {
    if (!webgpuState.available) {
      throw new Error(`WebGPU is unavailable: ${webgpuState.reason}`);
    }
    return [["webgpu"]];
  }
  if (backend === "wasm") {
    return [["wasm"]];
  }
  return webgpuState.available ? [["webgpu"], ["wasm"]] : [["wasm"]];
}

  // 将 wasmPaths/numThreads/simd/proxy 写入 ort.env.wasm
function applyOrtEnvironmentOptions(ort: OrtModule, ortOptions: OrtOptions): void {
  const wasmOptions = ort.env.wasm;

  if (ortOptions.wasmPaths !== undefined) {
    wasmOptions.wasmPaths = ortOptions.wasmPaths;
  }
  if (ortOptions.numThreads !== undefined) {
    wasmOptions.numThreads = ortOptions.numThreads;
  }
  if (ortOptions.simd !== undefined) {
    wasmOptions.simd = ortOptions.simd;
  }
  if (ortOptions.proxy !== undefined) {
    wasmOptions.proxy = ortOptions.proxy;
  }
  if (ortOptions.disableWasmProxy) {
    wasmOptions.proxy = false;
  }
}

  // 加载 ORT 模块、探测 WebGPU、应用环境选项并返回运行时摘要
export async function initOrtRuntime(
  ortOptions: OrtOptions | string = {}
): Promise<OrtRuntimeResult> {
  const backend =
    typeof ortOptions === "string"
      ? ortOptions
      : ortOptions.backend === "webgpu" || ortOptions.backend === "wasm"
        ? ortOptions.backend
        : "auto";
  const webgpuState = await detectWebGpuAvailability();
  const ort = await loadOrtModule();
  if (typeof ortOptions !== "string") {
    applyOrtEnvironmentOptions(ort, ortOptions);
  }
  return {
    ort,
    webgpuState,
    backend
  };
}

  // 按 provider 候选顺序尝试创建 InferenceSession，失败则回退下一组
export async function createSession(
  ort: OrtModule,
  modelBytes: Uint8Array,
  providerCandidates: string[][]
): Promise<SessionState> {
  let lastErr: unknown = null;
  for (const executionProviders of providerCandidates) {
    try {
      const session = await ort.InferenceSession.create(modelBytes, {
        executionProviders,
        graphOptimizationLevel: "all"
      });
      return { session, provider: executionProviders[0] };
    } catch (err: unknown) {
      lastErr = err;
    }
  }
  throw lastErr instanceof Error ? lastErr : new Error("Failed to create ONNX session.");
}

  // 并行调用 session.release() 释放 det/rec 等 ORT 会话资源
export async function releaseSessions(
  ...sessions: Array<import("onnxruntime-web").InferenceSession | null | undefined>
): Promise<void> {
  await Promise.all(
    sessions.map(async (session) => {
      if (!session?.release) return;
      await session.release();
    })
  );
}
