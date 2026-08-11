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

// OCR 单页结果：裁剪图 URL 与 prunedResult
export interface OCRPage {
  prunedResult: unknown;
  ocrImageUrl?: string;
  docPreprocessingImageUrl?: string;
  inputImageUrl?: string;
  raw?: unknown;
}

// 文档解析单页：Markdown 文本、图片与导出物
export interface DocParsingPage {
  markdownText: string;
  markdownImages: Record<string, string>;
  outputImages: Record<string, string>;
  prunedResult?: unknown;
  inputImageUrl?: string;
  exports?: Record<string, unknown>;
  markdown?: Record<string, unknown>;
  raw?: unknown;
}

// 完整 OCR 任务结果：jobId + 多页 pages
export interface OCRResult {
  jobId: string;
  pages: OCRPage[];
  dataInfo?: Record<string, unknown>;
}

// 完整文档解析任务结果
export interface DocParsingResult {
  jobId: string;
  pages: DocParsingPage[];
  dataInfo?: Record<string, unknown>;
}

// 异步任务进度：总页数/已提取页数与时间
export interface Progress {
  totalPages: number;
  extractedPages: number;
  startTime?: string;
  endTime?: string;
}

// 任务元信息：模型、任务类型与 batch 关联
export interface Job {
  jobId: string;
  model: string;
  task: "ocr" | "document_parsing";
  pageRanges?: string;
  batchId?: string;
}

// 单 job 状态：pending/running/done/failed
export interface JobStatus {
  jobId: string;
  state: "pending" | "running" | "done" | "failed";
  progress?: Progress;
  resultUrl?: Record<string, string>;
  errorMsg?: string;
}

// 批量任务状态：batchId 与子 job 列表
export interface BatchStatus {
  batchId: string;
  jobs: JobStatus[];
}
