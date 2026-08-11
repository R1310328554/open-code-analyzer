/*
 * Copyright (c) 2026 PaddlePaddle Authors. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

// 跨环境 Canvas 工厂：优先 OffscreenCanvas（worker），回退 DOM canvas
type AnyCanvas = OffscreenCanvas | HTMLCanvasElement;

  // 创建指定尺寸的 2D 画布，worker 与主线程自动选型
export function createCanvas(width: number, height: number): AnyCanvas {
  if (typeof OffscreenCanvas !== "undefined") {
    return new OffscreenCanvas(width, height);
  }
  const canvas = document.createElement("canvas");
  canvas.width = width;
  canvas.height = height;
  return canvas;
}

  // 获取 2d 上下文，失败时抛出明确错误
export function getContext2D(
  canvas: AnyCanvas
): CanvasRenderingContext2D | OffscreenCanvasRenderingContext2D {
  const ctx = canvas.getContext("2d");
  if (!ctx) {
    throw new Error("Failed to create 2D rendering context.");
  }
  return ctx;
}

  // 统一 OffscreenCanvas.convertToBlob 与 HTMLCanvasElement.toBlob
export function canvasToBlob(canvas: AnyCanvas, type: string, quality: number): Promise<Blob> {
  if (canvas instanceof OffscreenCanvas) {
    return canvas.convertToBlob({ type, quality });
  }
  return new Promise<Blob>((resolve, reject) => {
    canvas.toBlob(
      (blob) => {
        if (blob) {
          resolve(blob);
        } else {
          reject(new Error("canvas.toBlob() returned null."));
        }
      },
      type,
      quality
    );
  });
}
