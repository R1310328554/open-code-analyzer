/*
 * Copyright (c) 2026 PaddlePaddle Authors. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

// OCR 可视化类型：RGB 三元组与 Web Font 加载配置
/** An RGB color as a 3-element tuple of 0-255 integers. */
  // 0–255 整数 RGB 颜色，用于绘制检测框与文本 overlay
export type RgbColor = [number, number, number];

  // 自定义字体配置：family、URL/ArrayBuffer 源与 FontFace 描述符
export interface FontConfig {
  /** CSS font-family name. */
  family: string;
  /** Font source: URL string or ArrayBuffer. */
  source: string | ArrayBuffer;
  /** FontFace descriptors (weight, style, etc.). */
  descriptors?: FontFaceDescriptors;
}
