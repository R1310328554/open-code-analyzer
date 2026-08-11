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

package com.paddle.ocr.engine

// OCREngine 内部完整推理结果：含分阶段耗时与张量形状
import com.paddle.ocr.model.OCRResult

/**
 * 引擎层 OCR 结果：识别行列表 + 检测/识别各子阶段毫秒统计。
 */
data class OCREngineResult(
    // 通过 recScoreThresh 过滤后的文本行结果
    val results: List<OCRResult>,
    // 检测总耗时
    val detectionTimeMs: Long,
    // 识别总耗时（所有批次累加）
    val recognitionTimeMs: Long,
    val totalTimeMs: Long,
    val lineCount: Int,
    // 各子阶段耗时：预处理 / 推理 / 后处理
    // Detailed per-stage timing
    val detPreprocessMs: Long = 0,
    val detInferenceMs: Long = 0,
    val detPostprocessMs: Long = 0,
    val recPreprocessMs: Long = 0,
    val recInferenceMs: Long = 0,
    val recPostprocessMs: Long = 0,
    val pipelineOverheadMs: Long = 0,
    val coldLoadTimeMs: Long = 0,
    // 送入 ONNX 的 det/rec 输入张量形状，便于性能分析
    // Input tensor shapes
    val detInputShape: List<Int> = emptyList(),
    val recInputShapes: List<List<Int>> = emptyList(),
    // 逐行识别耗时，仅 recBatchSize==1 时填充
    // Per-line timing (only populated when recBatchSize == 1)
    val perLineRecMs: List<Long> = emptyList(),
)
