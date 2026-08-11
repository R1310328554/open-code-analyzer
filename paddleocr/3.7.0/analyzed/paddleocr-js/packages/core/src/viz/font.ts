/*
 * Copyright (c) 2026 PaddlePaddle Authors. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

// 自定义字体加载：通过 FontFace API 注册到 document.fonts
import type { FontConfig } from "./types";

  // 从 URL 或 ArrayBuffer 加载字体并 add 到 document.fonts
export async function loadFontFace(config: FontConfig): Promise<FontFace> {
  const source = typeof config.source === "string" ? `url(${config.source})` : config.source;

  const face = new FontFace(config.family, source, config.descriptors);
  await face.load();
  document.fonts.add(face);
  return face;
}

  // 从 document.fonts 移除已加载的 FontFace，配合 dispose 释放资源
export function removeFontFace(face: FontFace): void {
  document.fonts.delete(face);
}
