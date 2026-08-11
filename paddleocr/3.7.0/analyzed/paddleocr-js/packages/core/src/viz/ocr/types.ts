/*
 * Copyright (c) 2026 PaddlePaddle Authors. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

// OCR 可视化配置类型：检测框样式与 OcrVisualizer 全局选项
import type { RgbColor, FontConfig } from "../types";

  // 检测框绘制样式：填充透明度与按索引取色函数
export interface BoxStyleOptions {
  /** Fill opacity 0-1. Default: 0.5. */
  fillOpacity?: number;
  /** Custom color function. Default: deterministic LCG-based colors. */
  colorFn?: (index: number) => RgbColor;
}

  // OcrVisualizer 构造选项：字体、框样式、右侧面板背景与输出格式
export interface OcrVisualizerOptions {
  /** Custom font configuration. Falls back to system sans-serif if omitted. */
  font?: FontConfig;
  /** Detection box style overrides. */
  boxStyle?: BoxStyleOptions;
  /** Right panel background color. Default: "#ffffff". */
  textPanelBackground?: string;
  /** Output image format. Default: "png". */
  outputFormat?: "png" | "jpeg" | "webp";
  /** JPEG/WebP quality 0-1. Default: 0.92. */
  outputQuality?: number;
}
