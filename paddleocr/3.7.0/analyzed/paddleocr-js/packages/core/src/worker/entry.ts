/*
 * Copyright (c) 2026 PaddlePaddle Authors. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

// Worker 侧消息入口：将 transport 请求分派到业务 handler 并回传 success/error
import { createTransportError, createTransportSuccess, isTransportRequest } from "./protocol";

  // 业务处理器签名：按 type 路由 init/predict/dispose 等操作
export type MessageHandler = (type: string, payload: Record<string, unknown>) => Promise<unknown>;

interface WorkerLikeScope {
  onmessage: ((event: MessageEvent) => void) | null;
  postMessage(message: unknown): void;
}

  // 挂载 workerScope.onmessage，异步执行 handler 并用 requestId 回复
export function attachWorkerMessageHandler(
  handleMessage: MessageHandler,
  workerScope: WorkerLikeScope = self as unknown as WorkerLikeScope
): void {
  workerScope.onmessage = (event: MessageEvent) => {
    const message = event.data as unknown;
    if (!isTransportRequest(message)) {
      return;
    }

    void (async () => {
      try {
        const payload = await handleMessage(
          message.type,
          (message.payload || {}) as Record<string, unknown>
        );
        workerScope.postMessage(createTransportSuccess(message.requestId, payload));
      } catch (error: unknown) {
        workerScope.postMessage(createTransportError(message.requestId, error));
      }
    })();
  };
}
