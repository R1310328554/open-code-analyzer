// Copyright (c) 2026 PaddlePaddle Authors. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

import { JobFailedError, PollTimeoutError, RequestTimeoutError, ResponseFormatError } from "../errors.js";
import type { BatchStatus, JobStatus, Progress } from "../results.js";
import { throwIfAborted, userAbortReason } from "./abort.js";
import { HttpClient } from "./http.js";

// 异步任务轮询器：指数退避等待 job 完成并拉取 JSONL 结果

const INITIAL_INTERVAL = 3000;
const MULTIPLIER = 1.5;
const MAX_INTERVAL = 15000;
const MAX_WAIT_TIME = 600000;

// 轮询器：封装 getJobStatus 循环与批量状态聚合
export class Poller {
  private http: HttpClient;
  private maxWaitTime: number;

  // 绑定 HttpClient 与最大等待时长
  constructor(http: HttpClient, maxWaitTime: number = MAX_WAIT_TIME) {
    this.http = http;
    this.maxWaitTime = maxWaitTime;
  }

  // 阻塞轮询直到任务 done/failed 或超时
  async pollUntilDone(jobId: string, signal?: AbortSignal): Promise<unknown[]> {
    let interval = INITIAL_INTERVAL;
    const deadline = Date.now() + this.maxWaitTime;

    while (Date.now() < deadline) {
      throwIfAborted(signal);

      const remaining = deadline - Date.now();
      const data = await this.withPollTimeout(jobId, remaining, () => this.http.getJobStatus(jobId, signal, remaining));
      const status = normalizeStatus(jobId, data);

      if (status.state === "done") {
        const jsonUrl = resultJsonUrl(data);
        const resultRemaining = deadline - Date.now();
        return await this.withPollTimeout(jobId, resultRemaining, () => this.http.fetchJsonl(jsonUrl, signal, resultRemaining));
      }

      if (status.state === "failed") {
        throw new JobFailedError(jobId, status.errorMsg || "Unknown error");
      }

      await this.sleep(Math.min(interval, Math.max(0, deadline - Date.now())), signal);
      interval = Math.min(interval * MULTIPLIER, MAX_INTERVAL);
    }

    throw new PollTimeoutError(jobId, this.maxWaitTime);
  }

  // 单次查询并规范化为 JobStatus
  async getStatus(jobId: string, signal?: AbortSignal): Promise<JobStatus> {
    return normalizeStatus(jobId, await this.http.getJobStatus(jobId, signal));
  }

  // 解析 batch 响应中的 extractResult 数组
  async getBatchStatus(batchId: string, signal?: AbortSignal): Promise<BatchStatus> {
    const data = await this.http.getBatchStatus(batchId, signal);
    if (!isRecord(data) || !Array.isArray(data.extractResult)) {
      throw new ResponseFormatError("Batch response is missing extractResult.");
    }
    return {
      batchId,
      jobs: data.extractResult.map((item) => {
        if (!isRecord(item) || typeof item.jobId !== "string") {
          throw new ResponseFormatError("Batch extractResult item is missing jobId.");
        }
        return normalizeStatus(item.jobId, item);
      }),
    };
  }

  // 可中断的轮询间隔 sleep
  private sleep(ms: number, signal?: AbortSignal): Promise<void> {
    return new Promise((resolve, reject) => {
      const timer = setTimeout(resolve, ms);
      if (!signal) return;
      signal.addEventListener(
        "abort",
        () => {
          clearTimeout(timer);
          reject(userAbortReason(signal));
        },
        { once: true },
      );
    });
  }

  // 将单次 HTTP 超时映射为 PollTimeoutError
  private async withPollTimeout<T>(jobId: string, remainingMs: number, operation: () => Promise<T>): Promise<T> {
    if (remainingMs <= 0) {
      throw new PollTimeoutError(jobId, this.maxWaitTime);
    }
    try {
      return await operation();
    } catch (error) {
      if (error instanceof RequestTimeoutError && error.timeoutMs === remainingMs) {
        throw new PollTimeoutError(jobId, this.maxWaitTime, { cause: error });
      }
      throw error;
    }
  }
}

// 将原始 status JSON 规范化为 JobStatus 结构
function normalizeStatus(jobId: string, data: unknown): JobStatus {
  if (!isRecord(data) || typeof data.state !== "string") {
    throw new ResponseFormatError("Status response is missing state.");
  }
  if (!["pending", "running", "done", "failed"].includes(data.state)) {
    throw new ResponseFormatError(`Unknown job state: ${data.state}`);
  }
  return {
    jobId,
    state: data.state as JobStatus["state"],
    progress: normalizeProgress(data.extractProgress),
    resultUrl: isRecord(data.resultUrl) ? stringMap(data.resultUrl) : undefined,
    errorMsg: typeof data.errorMsg === "string" ? data.errorMsg : undefined,
  };
}

// 解析 extractProgress 中的页数与时间戳
function normalizeProgress(progress: unknown): Progress | undefined {
  if (progress === undefined || progress === null) {
    return undefined;
  }
  if (!isRecord(progress)) {
    throw new ResponseFormatError("Status progress must be an object.");
  }
  return {
    totalPages: typeof progress.totalPages === "number" ? progress.totalPages : 0,
    extractedPages: typeof progress.extractedPages === "number" ? progress.extractedPages : 0,
    startTime: typeof progress.startTime === "string" ? progress.startTime : undefined,
    endTime: typeof progress.endTime === "string" ? progress.endTime : undefined,
  };
}

// 从已完成任务中提取 resultUrl.jsonUrl
function resultJsonUrl(data: unknown): string {
  if (!isRecord(data) || !isRecord(data.resultUrl) || typeof data.resultUrl.jsonUrl !== "string") {
    throw new ResponseFormatError("Done job response is missing resultUrl.jsonUrl.");
  }
  return data.resultUrl.jsonUrl;
}

// 将 resultUrl 对象过滤为字符串键值对
function stringMap(value: Record<string, unknown>): Record<string, string> {
  const result: Record<string, string> = {};
  for (const [key, val] of Object.entries(value)) {
    if (typeof val === "string") {
      result[key] = val;
    }
  }
  return result;
}

// 类型守卫：判断是否为非 null 对象
function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null;
}
