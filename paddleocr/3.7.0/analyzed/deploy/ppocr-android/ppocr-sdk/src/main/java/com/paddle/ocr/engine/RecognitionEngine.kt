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

import com.paddle.ocr.postprocess.CTCDecoder
import com.paddle.ocr.preprocess.RecPreprocessor
// 文本识别引擎：Rec 预处理 → ONNX CTC 推理 → CTCDecoder 解码
import org.opencv.core.Mat

/**
 * CRNN+CTC 识别引擎：对裁剪后的文本行 Mat 批量识别并返回文本与置信度。
 */
class RecognitionEngine(
    private val ortManager: ORTSessionManager,
    private val characterList: List<String>,
) {
    // 识别结果：每行 (text, confidence) 与各阶段耗时
    data class RecognitionResult(
        val texts: List<Pair<String, Float>>,
        val preprocessMs: Long,
        val inferenceMs: Long,
        val postprocessMs: Long,
        val timeMs: Long,
        val inputShape: List<Int>,
    )

    // 批量识别：RecPreprocessor → runRecognition → CTCDecoder
    fun recognize(crops: List<Mat>): RecognitionResult {
        // 预处理：归一化、固定高度、宽度 padding 并拼 batch 张量
        // Preprocess
        val preStart = System.currentTimeMillis()
        val preResult = RecPreprocessor.preprocessBatch(crops)
        val preprocessMs = System.currentTimeMillis() - preStart

        // ONNX 识别模型前向推理
        // Inference
        val infStart = System.currentTimeMillis()
        val (outputData, outputShape) = ortManager.runRecognition(preResult.tensorData, preResult.shape)
        val inferenceMs = System.currentTimeMillis() - infStart

        // CTC 贪心解码：索引映射到 characterList 字符
        // Postprocess (CTC decode)
        val postStart = System.currentTimeMillis()
        val decoded = CTCDecoder.decode(outputData, outputShape, characterList)
        val postprocessMs = System.currentTimeMillis() - postStart

        val inputShape = preResult.shape.map { it.toInt() }
        val timeMs = preprocessMs + inferenceMs + postprocessMs
        return RecognitionResult(
            texts = decoded,
            preprocessMs = preprocessMs,
            inferenceMs = inferenceMs,
            postprocessMs = postprocessMs,
            timeMs = timeMs,
            inputShape = inputShape,
        )
    }
}
