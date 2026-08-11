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

import android.graphics.Bitmap
import com.paddle.ocr.PaddleOCRConfig
import com.paddle.ocr.model.OCRBox
import com.paddle.ocr.postprocess.DBPostProcessor
import com.paddle.ocr.preprocess.DetPreprocessResult
import com.paddle.ocr.preprocess.DetPreprocessor
// 文本检测引擎：Det 预处理 → ONNX DB 推理 → DBPostProcessor 后处理
import org.opencv.core.Mat

/**
 * DB 检测引擎：封装 ORTSessionManager 与 PaddleOCRConfig 完成文本区域定位。
 */
class DetectionEngine(
    private val ortManager: ORTSessionManager,
    private val config: PaddleOCRConfig,
) {
    // 检测结果：四边形框列表与各阶段毫秒耗时
    data class DetectionResult(
        val boxes: List<OCRBox>,
        val preprocessMs: Long,
        val inferenceMs: Long,
        val postprocessMs: Long,
        val timeMs: Long,
        val inputShape: List<Int>,
    )

    // 从 Android Bitmap 经 DetPreprocessor 缩放归一化后检测
    fun detect(bitmap: Bitmap): DetectionResult {
        return detect {
            DetPreprocessor.preprocess(
                bitmap,
                config.detLimitSideLen,
                config.detLimitType,
                config.detMaxSideLimit,
                config.detImgMode,
            )
        }
    }

    // 从 OpenCV BGR Mat 直接检测（与 OCREngine 流水线共用）
    fun detect(src: Mat): DetectionResult {
        return detect {
            DetPreprocessor.preprocess(
                src,
                config.detLimitSideLen,
                config.detLimitType,
                config.detMaxSideLimit,
                config.detImgMode,
            )
        }
    }

    // 统一检测流程：预处理计时 → runDetection → DB 后处理
    private fun detect(preprocessor: () -> DetPreprocessResult): DetectionResult {
        val preStart = System.currentTimeMillis()
        val preResult = preprocessor()
        val preprocessMs = System.currentTimeMillis() - preStart

        val infStart = System.currentTimeMillis()
        val (outputData, outputShape) = ortManager.runDetection(preResult.tensorData, preResult.shape)
        val inferenceMs = System.currentTimeMillis() - infStart

        val postStart = System.currentTimeMillis()
        val boxes = DBPostProcessor.process(
            pred = outputData,
            predShape = outputShape,
            thresh = config.detThresh,
            boxThresh = config.detBoxThresh,
            unclipRatio = config.detUnclipRatio,
            maxCandidates = config.detMaxCandidates,
            useDilation = config.detUseDilation,
            scoreMode = config.detScoreMode,
            boxType = config.detBoxType,
            originalH = preResult.originalH,
            originalW = preResult.originalW,
        )
        val postprocessMs = System.currentTimeMillis() - postStart

        return DetectionResult(
            boxes = boxes,
            preprocessMs = preprocessMs,
            inferenceMs = inferenceMs,
            postprocessMs = postprocessMs,
            timeMs = preprocessMs + inferenceMs + postprocessMs,
            inputShape = listOf(1, 3, preResult.shape[2].toInt(), preResult.shape[3].toInt()),
        )
    }
}
