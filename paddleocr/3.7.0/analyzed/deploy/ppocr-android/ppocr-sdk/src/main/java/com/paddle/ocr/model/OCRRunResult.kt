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

// OCRRunResult.kt — 一次完整 OCR 推理运行的汇总结果与分阶段耗时统计。

package com.paddle.ocr.modelpackage com.paddle.ocr.model

/**
 * OCRRunResult 封装检测+识别流水线的一次运行输出。
 * 包含各行识别结果、各阶段毫秒级耗时及输入张量形状等诊断信息。
 */
data class OCRRunResult(
    // results 按阅读顺序排列的逐行 OCR 识别结果列表。
    val results: List<OCRResult>,
    // detectionTimeMs 文本检测阶段总耗时（毫秒）。
    val detectionTimeMs: Long,
    // recognitionTimeMs 文本识别阶段总耗时（毫秒）。
    val recognitionTimeMs: Long,
    // totalTimeMs 端到端流水线总耗时（毫秒）。
    val totalTimeMs: Long,
    // lineCount 检测到的文本行数量。
    val lineCount: Int,
    // 以下为检测/识别各子阶段细分耗时（预处理、推理、后处理）。
    // Detailed per-stage timing
    val detPreprocessMs: Long = 0,
    val detInferenceMs: Long = 0,
    val detPostprocessMs: Long = 0,
    val recPreprocessMs: Long = 0,
    val recInferenceMs: Long = 0,
    val recPostprocessMs: Long = 0,
    // pipelineOverheadMs 流水线框架开销（调度、内存拷贝等）。
    val pipelineOverheadMs: Long = 0,
    // coldLoadTimeMs 冷启动时模型加载耗时。
    val coldLoadTimeMs: Long = 0,
    // 推理输入张量形状，便于性能分析与调试对齐。
    // Input tensor shapes
    val detInputShape: List<Int> = emptyList(),
    val recInputShapes: List<List<Int>> = emptyList(),
    // Per-line timing (only populated when recBatchSize == 1)
    // perLineRecMs 逐行识别耗时，仅在 recBatchSize==1 时填充。
    val perLineRecMs: List<Long> = emptyList(),
)
