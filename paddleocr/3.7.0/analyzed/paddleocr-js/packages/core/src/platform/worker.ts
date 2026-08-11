/*
 * Copyright (c) 2026 PaddlePaddle Authors. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

// Worker 线程平台层：ImageBitmap 载荷 → OpenCV Mat（OffscreenCanvas 路径）
import type { OpenCv, Mat } from "@techstark/opencv-js";
import type { SourceMatResult } from "./browser";
import { ensureServedFromHttp } from "./browser";

export interface WorkerSourcePayload {
  kind: "imageBitmap";
  imageBitmap: ImageBitmap;
}

  // OffscreenCanvas 绘制后 getImageData，worker 无 document
function imageBitmapToImageData(imageBitmap: ImageBitmap): ImageData {
  if (typeof OffscreenCanvas !== "function") {
    throw new Error("Worker mode requires OffscreenCanvas support in this browser.");
  }
  const canvas = new OffscreenCanvas(imageBitmap.width, imageBitmap.height);
  const ctx = canvas.getContext("2d", { willReadFrequently: true });
  if (!ctx) {
    throw new Error("Failed to create a 2D canvas context in the OCR worker.");
  }
  ctx.drawImage(imageBitmap, 0, 0);
  return ctx.getImageData(0, 0, imageBitmap.width, imageBitmap.height);
}

function imageDataToMat(cv: OpenCv, imageData: ImageData): Mat {
  return cv.matFromArray(imageData.height, imageData.width, cv.CV_8UC4, imageData.data);
}

function isWorkerSourcePayload(source: unknown): source is WorkerSourcePayload {
  if (typeof source !== "object" || source === null) return false;
  const candidate = source as Record<string, unknown>;
  return (
    candidate["kind"] === "imageBitmap" &&
    typeof ImageBitmap !== "undefined" &&
    candidate["imageBitmap"] instanceof ImageBitmap
  );
}

  // worker predict 输入适配：Mat clone 或 imageBitmap kind 解码
export function sourcePayloadToMat(cv: OpenCv, source: unknown): SourceMatResult {
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

  if (isWorkerSourcePayload(source)) {
    const imageData = imageBitmapToImageData(source.imageBitmap);
    const mat = imageDataToMat(cv, imageData);
    return {
      width: imageData.width,
      height: imageData.height,
      mat,
      dispose() {
        mat.delete();
        source.imageBitmap.close();
      }
    };
  }

  throw new Error("Unsupported worker image source payload.");
}

export { ensureServedFromHttp };
