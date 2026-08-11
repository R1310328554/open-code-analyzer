/*
 * Copyright (c) 2026 PaddlePaddle Authors. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

// 浏览器主线程平台层：DOM 图像源 → OpenCV Mat 与 Worker 可转移载荷
import type { OpenCv, Mat } from "@techstark/opencv-js";

export type ImageSource = ImageBitmap | Blob | HTMLCanvasElement | ImageData | HTMLImageElement;

  // sourceToMat 输出：宽高、Mat 与 dispose 释放回调
export interface SourceMatResult {
  width: number;
  height: number;
  mat: Mat;
  dispose(): void;
}

export interface WorkerPayload {
  kind: "imageBitmap";
  imageBitmap: ImageBitmap;
}

export interface WorkerPayloadResult {
  payload: WorkerPayload;
  transferables: Transferable[];
}

  // file:// 协议无法 fetch 模型，强制要求 HTTP(S) 部署
export function ensureServedFromHttp(): void {
  if (globalThis.location.protocol === "file:") {
    throw new Error("PaddleOCR.js requires an HTTP(S) origin so model assets can be fetched.");
  }
}

function hasDomConstructor(name: string): boolean {
  return typeof (globalThis as Record<string, unknown>)[name] !== "undefined";
}

  // 统一 Blob/Canvas/ImageData/img 为 ImageBitmap
export async function sourceToImageBitmap(source: ImageSource): Promise<ImageBitmap> {
  if (typeof ImageBitmap !== "undefined" && source instanceof ImageBitmap) return source;
  if (source instanceof Blob) return createImageBitmap(source);
  if (hasDomConstructor("HTMLCanvasElement") && source instanceof HTMLCanvasElement) {
    return createImageBitmap(source);
  }
  if (source instanceof ImageData) {
    const canvas = document.createElement("canvas");
    canvas.width = source.width;
    canvas.height = source.height;
    const ctx = canvas.getContext("2d");
    if (!ctx) throw new Error("Failed to create a 2D canvas context.");
    ctx.putImageData(source, 0, 0);
    return createImageBitmap(canvas);
  }
  if (hasDomConstructor("HTMLImageElement") && source instanceof HTMLImageElement) {
    return createImageBitmap(source);
  }
  throw new Error("Unsupported image source. Use a Blob, ImageBitmap, ImageData, canvas, or img.");
}

async function sourceToClonedImageBitmap(source: ImageSource): Promise<ImageBitmap> {
  if (typeof ImageBitmap !== "undefined" && source instanceof ImageBitmap) {
    return createImageBitmap(source);
  }
  return sourceToImageBitmap(source);
}

  // Canvas 绘制 ImageBitmap 后 cv.imread 得到 BGR Mat
export function bitmapToSourceMat(
  cv: OpenCv,
  imageBitmap: ImageBitmap
): { canvas: HTMLCanvasElement; mat: Mat } {
  const canvas = document.createElement("canvas");
  canvas.width = imageBitmap.width;
  canvas.height = imageBitmap.height;
  const ctx = canvas.getContext("2d", { willReadFrequently: true });
  if (!ctx) throw new Error("Failed to create a 2D canvas context.");
  ctx.drawImage(imageBitmap, 0, 0);
  return {
    canvas,
    mat: cv.imread(canvas)
  };
}

  // 主线程 OCR 输入适配：Mat 直接 clone，否则经 ImageBitmap→Mat
export async function sourceToMat(cv: OpenCv, source: unknown): Promise<SourceMatResult> {
  if (typeof cv.Mat === "function" && source instanceof cv.Mat) {
    const cloned = source.clone();
    return {
      width: source.cols,
      height: source.rows,
      mat: cloned,
      dispose() {
        cloned.delete();
      }
    };
  }

  const imageBitmap = await sourceToImageBitmap(source as ImageSource);
  const sourceImage = bitmapToSourceMat(cv, imageBitmap);
  return {
    width: imageBitmap.width,
    height: imageBitmap.height,
    mat: sourceImage.mat,
    dispose() {
      sourceImage.mat.delete();
      imageBitmap.close();
    }
  };
}

  // Worker 模式：克隆 ImageBitmap 并列入 transferables
export async function sourceToWorkerPayload(source: ImageSource): Promise<WorkerPayloadResult> {
  if (typeof ImageBitmap === "undefined" || typeof createImageBitmap !== "function") {
    throw new Error("Worker mode requires ImageBitmap support in this browser.");
  }
  const imageBitmap = await sourceToClonedImageBitmap(source);
  return {
    payload: {
      kind: "imageBitmap",
      imageBitmap
    },
    transferables: [imageBitmap]
  };
}
