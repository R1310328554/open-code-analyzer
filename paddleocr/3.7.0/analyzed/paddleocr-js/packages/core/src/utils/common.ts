/*
 * Copyright (c) 2026 PaddlePaddle Authors. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

// 通用工具函数：计时、数值裁剪、距离、超时与批处理辅助
export function nowMs(): number {
  return performance.now();
}

  // 将 value 限制在 [min, max] 区间内
export function clamp(value: number, min: number, max: number): number {
  return Math.max(min, Math.min(max, value));
}

  // 计算二维欧氏距离
export function distance2(p0: [number, number], p1: [number, number]): number {
  const dx = p0[0] - p1[0];
  const dy = p0[1] - p1[1];
  return Math.sqrt(dx * dx + dy * dy);
}

  // 将毫秒数格式化为带一位小数的可读字符串
export function formatMs(value: number): string {
  return `${value.toFixed(1)} ms`;
}

  // 为 Promise 附加超时拒绝，label 用于错误信息
export function withTimeout<T>(promise: Promise<T>, ms: number, label: string): Promise<T> {
  let settled = false;
  return new Promise<T>((resolve, reject) => {
    const timer = setTimeout(() => {
      if (settled) return;
      settled = true;
      reject(new Error(`${label} timed out after ${String(ms / 1000)}s`));
    }, ms);

    promise
      .then((result) => {
        if (settled) return;
        settled = true;
        clearTimeout(timer);
        resolve(result);
      })
      .catch((err: unknown) => {
        if (settled) return;
        settled = true;
        clearTimeout(timer);
        // eslint-disable-next-line @typescript-eslint/prefer-promise-reject-errors -- propagating upstream rejection
        reject(err);
      });
  });
}

  // 解析运行时 batch_size 覆盖值，非法时回退为至少 1
export function resolveRuntimeBatchSize(override: unknown, defaultBatchSize: number): number {
  const rawBatch = override ?? defaultBatchSize;
  const coercedBatch =
    typeof rawBatch === "number"
      ? rawBatch
      : typeof rawBatch === "string"
        ? Number.parseInt(rawBatch, 10)
        : Number.NaN;
  return Math.max(1, Number.isFinite(coercedBatch) ? coercedBatch : 1);
}

  // 将数组按固定 size 切分为连续子块
export function chunkArray<T>(items: T[], size: number): T[][] {
  const chunks: T[][] = [];
  for (let i = 0; i < items.length; i += size) {
    chunks.push(items.slice(i, i + size));
  }
  return chunks;
}

  // 基于 structuredClone 的深拷贝
export function deepClone<T>(value: T): T {
  return structuredClone(value);
}
