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

package com.paddle.ocr.demo

import android.app.Application
import android.util.Log
import com.paddle.ocr.PaddleOCR
import com.paddle.ocr.PaddleOCRConfig
import com.paddle.ocr.util.OpenCVUtils
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * OCRApplication — 全局 Application：后台加载 PaddleOCR 模型并暴露加载状态。
 * 通过 [modelState] StateFlow 向 UI 层广播 Loading / Ready / Error。
 */
class OCRApplication : Application() {class OCRApplication : Application() {

    /** ModelState 密封类：描述 OCR 模型加载过程中的三种状态。 */
    sealed class ModelState {
        // Loading — 模型正在后台加载。
        data object Loading : ModelState()
        // Ready — 加载成功，持有可用的 PaddleOCR 实例。
        data class Ready(val ocr: PaddleOCR) : ModelState()
        // Error — 加载失败，携带可读错误信息。
        data class Error(val message: String) : ModelState()
    }

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _modelState = MutableStateFlow<ModelState>(ModelState.Loading)

    val modelState: StateFlow<ModelState> = _modelState.asStateFlow()

    val ocr: PaddleOCR?
        get() = (_modelState.value as? ModelState.Ready)?.ocr

    val isModelLoaded: Boolean
        get() = _modelState.value is ModelState.Ready

    // onCreate 保存单例引用并触发首次模型加载。
    override fun onCreate() {
        super.onCreate()
        instance = this
        loadModels()
    }

    // retryLoadModels 在非 Loading 状态下重新发起模型加载。
    fun retryLoadModels() {
        if (_modelState.value is ModelState.Loading) return
        loadModels()
    }

    // loadModels 在 IO 协程中初始化 OpenCV 并创建 PaddleOCR 实例。
    private fun loadModels() {
        _modelState.value = ModelState.Loading
        applicationScope.launch {
            try {
                if (!OpenCVUtils.init(this@OCRApplication)) {
                    throw IllegalStateException("Failed to initialize OpenCV native library")
                }
                val loadedOcr = PaddleOCR.create(
                    context = this@OCRApplication,
                    config = PaddleOCRConfig(
                        recScoreThresh = 0.0f,
                        recBatchSize = 1,
                    ),
                )
                _modelState.value = ModelState.Ready(loadedOcr)
            } catch (t: Throwable) {
                if (t is CancellationException) throw t
                val message = modelLoadErrorMessage(t)
                Log.e(TAG, message, t)
                _modelState.value = ModelState.Error(message)
            }
        }
    }

    // modelLoadErrorMessage 拼接异常链消息供 UI 展示。
    private fun modelLoadErrorMessage(error: Throwable): String {
        val details = listOfNotNull(error.message, error.cause?.message)
            .distinct()
            .joinToString(": ")
        return if (details.isBlank()) {
            "Failed to load OCR models"
        } else {
            "Failed to load OCR models: $details"
        }
    }

    // onTerminate 释放 PaddleOCR 原生资源（调试/模拟器场景）。
    override fun onTerminate() {
        super.onTerminate()
        applicationScope.launch {
            ocr?.release()
        }
    }

    // companion object 提供进程级单例 [instance] 供 Activity/Compose 访问。
    companion object {
        private const val TAG = "OCRApplication"

        lateinit var instance: OCRApplication
            private set
    }
}
