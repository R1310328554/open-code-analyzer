/*
 * Copyright (c) 2026 PaddlePaddle Authors. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

// 可视化配色：基于 LCG 伪随机为每个检测框生成确定性 RGB 颜色
import type { RgbColor } from "./types";

  // 输入框索引，输出 [r,g,b] 三元组（0-255），同索引颜色可复现
export function deterministicColor(index: number): RgbColor {
  let seed = (index + 1) * 1103515245 + 12345;
  seed >>>= 0;
  const r = (seed >> 16) & 0xff;
  seed = (seed * 1103515245 + 12345) >>> 0;
  const g = (seed >> 16) & 0xff;
  seed = (seed * 1103515245 + 12345) >>> 0;
  const b = (seed >> 16) & 0xff;
  return [r, g, b];
}
