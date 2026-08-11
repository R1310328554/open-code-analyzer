/*
 * Copyright (c) 2026 PaddlePaddle Authors. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

// 可视化子模块 barrel：OCR 并排渲染 API 与配色工具重导出
export { OcrVisualizer, renderOcrToBlob } from "./ocr/renderer";
export { deterministicColor } from "./color";

// 通用 viz 类型：RGB 元组与 FontConfig
export type { RgbColor, FontConfig } from "./types";
// OCR 可视化选项：检测框样式与输出格式配置
export type { BoxStyleOptions, OcrVisualizerOptions } from "./ocr/types";
