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

// results.go — OCR/文档解析结果、任务状态与批处理状态的类型定义。

// results.go — OCR/文档解析结果、任务状态与批处理状态的类型定义。

package paddleocr

// OCRPage 单页 OCR 结果：裁剪结果、图像 URL 及原始 JSON。
// OCRPage 单页 OCR 结果：裁剪结果、图像 URL 及原始 JSON。
type OCRPage struct {
	PrunedResult             interface{}            `json:"prunedResult"`
	OCRImageURL              string                 `json:"ocrImageUrl,omitempty"`
	DocPreprocessingImageURL string                 `json:"docPreprocessingImageUrl,omitempty"`
	InputImageURL            string                 `json:"inputImageUrl,omitempty"`
	Raw                      map[string]interface{} `json:"raw,omitempty"`
}

// DocParsingPage 单页文档解析结果：Markdown 文本、图片映射与导出。
// DocParsingPage 单页文档解析结果：Markdown 文本、图片映射与导出。
type DocParsingPage struct {
	MarkdownText   string                 `json:"markdownText"`
	MarkdownImages map[string]string      `json:"markdownImages"`
	OutputImages   map[string]string      `json:"outputImages"`
	PrunedResult   interface{}            `json:"prunedResult,omitempty"`
	InputImageURL  string                 `json:"inputImageUrl,omitempty"`
	Exports        map[string]interface{} `json:"exports,omitempty"`
	Markdown       map[string]interface{} `json:"markdown,omitempty"`
	Raw            map[string]interface{} `json:"raw,omitempty"`
}

// OCRResult 完整 OCR 任务结果，含 JobID、各页与 dataInfo。
// OCRResult 完整 OCR 任务结果，含 JobID、各页与 dataInfo。
type OCRResult struct {
	JobID    string                 `json:"jobId"`
	Pages    []OCRPage              `json:"pages"`
	DataInfo map[string]interface{} `json:"dataInfo,omitempty"`
}

// DocParsingResult 完整文档解析任务结果。
// DocParsingResult 完整文档解析任务结果。
type DocParsingResult struct {
	JobID    string                 `json:"jobId"`
	Pages    []DocParsingPage       `json:"pages"`
	DataInfo map[string]interface{} `json:"dataInfo,omitempty"`
}

// Progress 任务提取进度（总页数、已提取页数、起止时间）。
// Progress 任务提取进度（总页数、已提取页数、起止时间）。
type Progress struct {
	TotalPages     int    `json:"totalPages"`
	ExtractedPages int    `json:"extractedPages"`
	StartTime      string `json:"startTime,omitempty"`
	EndTime        string `json:"endTime,omitempty"`
}

// Job 已提交任务的元数据（JobID、模型、任务类型、页范围、批次 ID）。
// Job 已提交任务的元数据（JobID、模型、任务类型、页范围、批次 ID）。
type Job struct {
	JobID      string `json:"jobId"`
	Model      string `json:"model"`
	Task       string `json:"task"`
	PageRanges string `json:"pageRanges,omitempty"`
	BatchID    string `json:"batchId,omitempty"`
}

// JobStatus 任务当前状态：pending/running/done/failed 及结果 URL。
// JobStatus 任务当前状态：pending/running/done/failed 及结果 URL。
type JobStatus struct {
	JobID     string            `json:"jobId"`
	State     string            `json:"state"`
	Progress  *Progress         `json:"progress,omitempty"`
	ResultURL map[string]string `json:"resultUrl,omitempty"`
	ErrorMsg  string            `json:"errorMsg,omitempty"`
}

// BatchStatus 同一 batchID 下所有子任务的状态列表。
// BatchStatus 同一 batchID 下所有子任务的状态列表。
type BatchStatus struct {
	BatchID string       `json:"batchId"`
	Jobs    []*JobStatus `json:"jobs"`
}
