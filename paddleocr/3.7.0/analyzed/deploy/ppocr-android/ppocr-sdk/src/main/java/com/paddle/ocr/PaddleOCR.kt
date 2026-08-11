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

package com.paddle.ocr

import android.content.Context
import android.graphics.Bitmap
import com.paddle.ocr.engine.OCREngine
import com.paddle.ocr.engine.OCREngineResult
import com.paddle.ocr.model.OCRRunResult
import com.paddle.ocr.model.OCRError
import kotlinx.coroutines.Dispatchers
// PaddleOCR Android SDK 公开入口：封装 OCREngine 并提供协程友好 API
import kotlinx.coroutines.withContext

/**
 * PaddleOCR 门面类：通过 companion create 工厂加载模型，recognize 执行检测+识别。
 */
class PaddleOCR private constructor(
    private val engine: OCREngine,
) {
    /** 冷启动加载 ONNX 检测/识别模型耗时（毫秒）。 */
    /** Time spent loading ONNX models (milliseconds). */
    val coldLoadTimeMs: Long get() = engine.coldLoadTimeMs

    // 工厂方法：在 IO 调度器上构造 OCREngine 并返回 PaddleOCR 实例
    companion object {

        // 使用默认 PaddleOCRConfig 与 EngineConfig 创建实例
        suspend fun create(context: Context): PaddleOCR {
            val appContext = context.applicationContext
            return withContext(Dispatchers.IO) {
                val engine = OCREngine(appContext, PaddleOCRConfig(), EngineConfig())
                PaddleOCR(engine)
            }
        }

        // 自定义检测/识别阈值与批大小等推理参数
        suspend fun create(
            context: Context,
            config: PaddleOCRConfig,
            engineConfig: EngineConfig = EngineConfig(),
        ): PaddleOCR {
            val appContext = context.applicationContext
            return withContext(Dispatchers.IO) {
                val engine = OCREngine(appContext, config, engineConfig)
                PaddleOCR(engine)
            }
        }

        // 指定 assets 中 det/rec ONNX 与 rec YAML 路径
        suspend fun create(
            context: Context,
            config: PaddleOCRConfig,
            engineConfig: EngineConfig,
            detModelAssetPath: String,
            recModelAssetPath: String,
            recConfigAssetPath: String,
        ): PaddleOCR {
            val appContext = context.applicationContext
            return withContext(Dispatchers.IO) {
                val engine = OCREngine(
                    appContext, config, engineConfig,
                    detModelAsset = detModelAssetPath,
                    recModelAsset = recModelAssetPath,
                    recConfigAsset = recConfigAssetPath,
                )
                PaddleOCR(engine)
            }
        }
    }

    // 对 Bitmap 执行 OCR，空尺寸抛出 InvalidImage
    suspend fun recognize(bitmap: Bitmap): OCRRunResult {
        if (bitmap.width == 0 || bitmap.height == 0) {
            throw OCRError.InvalidImage()
        }
        return recognizeResult { engine.run(bitmap) }
    }

    // 对 JPEG/PNG 字节流解码并识别
    suspend fun recognize(imageBytes: ByteArray): OCRRunResult {
        if (imageBytes.isEmpty()) {
            throw OCRError.InvalidImage()
        }
        return recognizeResult { engine.run(imageBytes) }
    }

    // 在 IO 线程运行引擎并将 OCREngineResult 映射为 OCRRunResult
    private suspend fun recognizeResult(runEngine: () -> OCREngineResult): OCRRunResult {
        return withContext(Dispatchers.IO) {
            val result = runEngine()
            OCRRunResult(
                results = result.results,
                detectionTimeMs = result.detectionTimeMs,
                recognitionTimeMs = result.recognitionTimeMs,
                totalTimeMs = result.totalTimeMs,
                lineCount = result.lineCount,
                detPreprocessMs = result.detPreprocessMs,
                detInferenceMs = result.detInferenceMs,
                detPostprocessMs = result.detPostprocessMs,
                recPreprocessMs = result.recPreprocessMs,
                recInferenceMs = result.recInferenceMs,
                recPostprocessMs = result.recPostprocessMs,
                pipelineOverheadMs = result.pipelineOverheadMs,
                coldLoadTimeMs = result.coldLoadTimeMs,
                detInputShape = result.detInputShape,
                recInputShapes = result.recInputShapes,
                perLineRecMs = result.perLineRecMs,
            )
        }
    }

    // 释放 ONNX Session 与相关 native 资源
    suspend fun release() {
        withContext(Dispatchers.IO) {
            engine.release()
        }
    }
}
