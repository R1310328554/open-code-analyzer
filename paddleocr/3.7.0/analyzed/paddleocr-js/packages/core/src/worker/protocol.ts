/*
 * Copyright (c) 2026 PaddlePaddle Authors. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

// Worker 主线程 RPC 协议：kind 区分请求/响应，requestId 关联异步回调
const REQUEST_KIND = "worker-transport-request";
const RESPONSE_KIND = "worker-transport-response";

export interface SerializedError {
  name: string;
  message: string;
  stack: string;
}

  // 主线程 → worker 请求：type、payload 与递增 requestId
export interface TransportRequest {
  kind: typeof REQUEST_KIND;
  type: string;
  payload: unknown;
  requestId: number;
}

export interface TransportSuccessResponse {
  kind: typeof RESPONSE_KIND;
  status: "success";
  requestId: number;
  payload: unknown;
}

export interface TransportErrorResponse {
  kind: typeof RESPONSE_KIND;
  status: "error";
  requestId: number;
  error: SerializedError;
}

  // 响应联合体：success 携带 payload，error 携带 SerializedError
export type TransportResponse = TransportSuccessResponse | TransportErrorResponse;

  // 构造标准 transport 请求对象
export function createTransportRequest(
  type: string,
  payload: unknown,
  requestId: number
): TransportRequest {
  return {
    kind: REQUEST_KIND,
    type,
    payload,
    requestId
  };
}

export function createTransportSuccess(
  requestId: number,
  payload: unknown
): TransportSuccessResponse {
  return {
    kind: RESPONSE_KIND,
    status: "success",
    requestId,
    payload
  };
}

export function createTransportError(requestId: number, error: unknown): TransportErrorResponse {
  return {
    kind: RESPONSE_KIND,
    status: "error",
    requestId,
    error: serializeError(error)
  };
}

  // 类型守卫：校验 kind 是否为 worker-transport-request
export function isTransportRequest(message: unknown): message is TransportRequest {
  return (
    typeof message === "object" &&
    message !== null &&
    "kind" in message &&
    message.kind === REQUEST_KIND
  );
}

export function isTransportResponse(message: unknown): message is TransportResponse {
  return (
    typeof message === "object" &&
    message !== null &&
    "kind" in message &&
    message.kind === RESPONSE_KIND
  );
}

  // 将异常序列化为可结构化克隆的 name/message/stack
export function serializeError(error: unknown): SerializedError {
  const err = error as Partial<Error> | undefined;
  return {
    name: err?.name || "Error",
    message: err?.message || "Unknown worker error.",
    stack: err?.stack || ""
  };
}

  // 在 main thread 重建 Error 实例并恢复 stack
export function deserializeError(error: unknown): Error {
  const normalized = (error || {}) as SerializedError;
  const instance = new Error(normalized.message || "Unknown worker error.");
  instance.name = normalized.name || "Error";
  if (normalized.stack) {
    instance.stack = normalized.stack;
  }
  return instance;
}
