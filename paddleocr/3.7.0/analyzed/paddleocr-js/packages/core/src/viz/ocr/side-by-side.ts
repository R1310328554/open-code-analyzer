/*
 * Copyright (c) 2026 PaddlePaddle Authors. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

// 并排视图合成：左侧检测框叠加 + 右侧文本面板，输出 Canvas/ImageBitmap/Blob
import type { OcrResult } from "../../pipelines/ocr/core";
import type { BoxStyleOptions } from "./types";
import { drawBoxesPanel } from "./draw-boxes";
import { drawTextPanel } from "./draw-text";
import { createCanvas, getContext2D, canvasToBlob } from "../canvas-factory";

type DrawableImage = ImageBitmap | HTMLImageElement;

  // 统一 ImageBitmap 与 HTMLImageElement 的宽度读取
function imageWidth(image: DrawableImage): number {
  return image instanceof HTMLImageElement ? image.naturalWidth : image.width;
}

function imageHeight(image: DrawableImage): number {
  return image instanceof HTMLImageElement ? image.naturalHeight : image.height;
}

  // 并排渲染解析选项：框样式、字体、背景色与输出编码参数
export interface SideBySideOptions {
  boxStyle: BoxStyleOptions;
  fontFamily: string;
  textPanelBackground: string;
  outputFormat: string;
  outputQuality: number;
}

  // 创建 2×宽画布，依次调用 drawBoxesPanel 与 drawTextPanel
export function renderSideBySideToCanvas(
  image: DrawableImage,
  result: OcrResult,
  options: SideBySideOptions
): OffscreenCanvas | HTMLCanvasElement {
  const w = imageWidth(image);
  const h = imageHeight(image);
  const canvas = createCanvas(w * 2, h);
  const ctx = getContext2D(canvas);

  drawBoxesPanel(ctx, image, result.items, options.boxStyle);
  drawTextPanel(
    ctx,
    w,
    h,
    result.items,
    options.boxStyle,
    options.fontFamily,
    options.textPanelBackground
  );

  return canvas;
}

  // Canvas → createImageBitmap 零拷贝位图输出
export async function renderSideBySideToImageBitmap(
  image: DrawableImage,
  result: OcrResult,
  options: SideBySideOptions
): Promise<ImageBitmap> {
  const canvas = renderSideBySideToCanvas(image, result, options);
  return createImageBitmap(canvas as ImageBitmapSource);
}

  // Canvas → canvasToBlob，按 outputFormat/quality 编码
export async function renderSideBySideToBlob(
  image: DrawableImage,
  result: OcrResult,
  options: SideBySideOptions
): Promise<Blob> {
  const canvas = renderSideBySideToCanvas(image, result, options);
  return canvasToBlob(canvas, `image/${options.outputFormat}`, options.outputQuality);
}
