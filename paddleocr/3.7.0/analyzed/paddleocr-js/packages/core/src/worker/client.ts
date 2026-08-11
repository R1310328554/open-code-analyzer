/*
 * Copyright (c) 2026 PaddlePaddle Authors. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

// 主线程 Worker 传输客户端：request/response 关联、错误反序列化与生命周期管理
import { createTransportRequest, deserializeError, isTransportResponse } from "./protocol";

interface PendingRequest {
  resolve: (value: unknown) => void;
  reject: (reason: unknown) => void;
}

  // 可选 createWorker 工厂，用于注入自定义 module worker 入口
export interface WorkerOptions {
  createWorker?: () => Worker;
}

  // 封装 postMessage 往返：按 requestId 匹配 pending Promise
export class WorkerTransportClient {
  private workerOptions: WorkerOptions;
  private worker: Worker | null;
  private pending: Map<number, PendingRequest>;
  private nextRequestId: number;
  private disposed: boolean;

  constructor(workerOptions: WorkerOptions = {}) {
    this.workerOptions = workerOptions;
    this.worker = null;
    this.pending = new Map();
    this.nextRequestId = 1;
    this.disposed = false;
  }

  ensureActive(): void {
    if (this.disposed) {
      throw new Error("Worker transport client has been disposed.");
    }
  }

    // 懒创建 worker 并挂载 onmessage/onerror，复用单例实例
  ensureWorker(): Worker {
    this.ensureActive();
    if (this.worker) {
      return this.worker;
    }

    const workerFactory = this.workerOptions.createWorker;
    if (typeof workerFactory !== "function") {
      throw new Error("Worker transport client requires a createWorker() factory.");
    }
    const worker = workerFactory();
    worker.onmessage = (event: MessageEvent) => {
      const message = event.data as unknown;
      if (!isTransportResponse(message)) return;
      const pending = this.pending.get(message.requestId);
      if (!pending) return;
      this.pending.delete(message.requestId);
      if (message.status === "success") {
        pending.resolve(message.payload);
      } else {
        pending.reject(deserializeError(message.error));
      }
    };
    worker.onerror = (event: ErrorEvent) => {
      const error = new Error(event.message || "OCR worker failed.");
      for (const pending of this.pending.values()) {
        pending.reject(error);
      }
      this.pending.clear();
    };
    this.worker = worker;
    return worker;
  }

    // 发送 transport 请求，支持 ImageBitmap 等 transferables 零拷贝
  request(type: string, payload: unknown, transferables: Transferable[] = []): Promise<unknown> {
    const worker = this.ensureWorker();
    const requestId = this.nextRequestId;
    this.nextRequestId += 1;

    return new Promise((resolve, reject) => {
      this.pending.set(requestId, { resolve, reject });
      worker.postMessage(createTransportRequest(type, payload, requestId), transferables);
    });
  }

  disposeWorker(): void {
    if (!this.worker) {
      return;
    }
    this.worker.terminate();
    this.worker = null;
  }

    // 标记 disposed、reject 全部 pending 并 terminate worker
  dispose(): void {
    if (this.disposed) {
      return;
    }
    this.disposed = true;
    for (const pending of this.pending.values()) {
      pending.reject(new Error("Worker transport client has been disposed."));
    }
    this.pending.clear();
    this.disposeWorker();
  }
}

  // 工厂函数：构造 WorkerTransportClient 实例
export function createWorkerTransportClient(workerOptions: WorkerOptions): WorkerTransportClient {
  return new WorkerTransportClient(workerOptions);
}
