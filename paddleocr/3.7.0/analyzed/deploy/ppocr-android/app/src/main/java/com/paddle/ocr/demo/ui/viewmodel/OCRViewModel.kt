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

package com.paddle.ocr.demo.ui.viewmodel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paddle.ocr.demo.OCRApplication
import com.paddle.ocr.model.OCRResult
import com.paddle.ocr.model.OCRRunResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
// OCR Demo ViewModel：管理 UI 状态、图片解码与 PaddleOCR 推理调用
import kotlinx.coroutines.withContext

/**
 * 首页 ViewModel：订阅 Application 模型状态，驱动选图与 recognize 流程。
 */
class OCRViewModel : ViewModel() {

    // 界面状态机：Loading / Ready / Processing / Result / Error
    sealed class UIState {
        data object Loading : UIState()
        data object Ready : UIState()
        data class Processing(val bitmap: Bitmap) : UIState()
        data class Result(val bitmap: Bitmap, val result: OCRRunResult) : UIState()
        data class Error(val message: String) : UIState()
    }

    // 各阶段耗时：检测、识别与总耗时（毫秒）
    data class TimingInfo(
        val detectionMs: Long,
        val recognitionMs: Long,
        val totalMs: Long,
    )

    private val _uiState = MutableStateFlow<UIState>(
        uiStateForModelState(OCRApplication.instance.modelState.value)
    )
    val uiState: StateFlow<UIState> = _uiState.asStateFlow()

    private val _timing = MutableStateFlow<TimingInfo?>(null)
    val timing: StateFlow<TimingInfo?> = _timing.asStateFlow()

    // 将 Application.ModelState 映射为 UIState
    private fun uiStateForModelState(modelState: OCRApplication.ModelState): UIState {
        return when (modelState) {
            is OCRApplication.ModelState.Loading -> UIState.Loading
            is OCRApplication.ModelState.Ready -> UIState.Ready
            is OCRApplication.ModelState.Error -> UIState.Error(modelState.message)
        }
    }

    // 监听全局模型加载状态，同步 Loading / Ready / Error
    init {
        viewModelScope.launch {
            OCRApplication.instance.modelState.collect { modelState ->
                when (modelState) {
                    is OCRApplication.ModelState.Loading -> _uiState.value = UIState.Loading
                    is OCRApplication.ModelState.Ready -> {
                        if (_uiState.value is UIState.Loading || _uiState.value is UIState.Error) {
                            _uiState.value = UIState.Ready
                        }
                    }
                    is OCRApplication.ModelState.Error -> _uiState.value = UIState.Error(modelState.message)
                }
            }
        }
    }

    // 从 ContentResolver 读取 URI 字节并采样解码后识别
    fun onImageSelected(uri: Uri) {
        viewModelScope.launch {
            try {
                val bytes = withContext(Dispatchers.IO) {
                    OCRApplication.instance.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                }
                if (bytes != null) {
                    val bitmap = withContext(Dispatchers.IO) {
                        decodeSampledBitmap(bytes, maxWidth = 2048, maxHeight = 2048)
                    }
                    if (bitmap != null) {
                        processImageBytes(bytes, bitmap)
                    } else {
                        _uiState.value = UIState.Error("Failed to load image")
                    }
                } else {
                    _uiState.value = UIState.Error("Failed to load image")
                }
            } catch (e: Exception) {
                _uiState.value = UIState.Error(e.message ?: "Unknown error")
            }
        }
    }

    // 从 raw 资源加载内置示例图并识别
    fun onSampleImageClicked(resId: Int) {
        viewModelScope.launch {
            val bytes = withContext(Dispatchers.IO) {
                OCRApplication.instance.resources.openRawResource(resId).use { it.readBytes() }
            }
            val bitmap = withContext(Dispatchers.IO) {
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            }
            if (bitmap != null) {
                processImageBytes(bytes, bitmap)
            } else {
                _uiState.value = UIState.Error("Failed to load image")
            }
        }
    }

    // 切换 Processing 状态，调用 OCRApplication.ocr.recognize
    private suspend fun processImageBytes(bytes: ByteArray, bitmap: Bitmap) {
        _uiState.value = UIState.Processing(bitmap)

        try {
            val ocr = OCRApplication.instance.ocr
                ?: throw IllegalStateException("OCR engine not initialized")

            val result = ocr.recognize(bytes)
            _uiState.value = UIState.Result(bitmap, result)
            _timing.value = TimingInfo(
                detectionMs = result.detectionTimeMs,
                recognitionMs = result.recognitionTimeMs,
                totalMs = result.totalTimeMs,
            )
        } catch (e: Exception) {
            _uiState.value = UIState.Error(e.message ?: "OCR failed")
        }
    }

    // 错误后重试：已加载则回 Ready，否则触发 Application 重新加载模型
    fun retry() {
        val app = OCRApplication.instance
        if (app.isModelLoaded) {
            _uiState.value = UIState.Ready
        } else {
            app.retryLoadModels()
        }
    }

    // 将全部识别文本拼接后写入系统剪贴板
    fun copyAllResults(results: List<OCRResult>) {
        val text = results.joinToString("\n") { it.text }
        val clipboard = OCRApplication.instance.getSystemService(android.content.Context.CLIPBOARD_SERVICE)
                as android.content.ClipboardManager
        clipboard.setPrimaryClip(android.content.ClipData.newPlainText("OCR Results", text))
    }

    // 两遍解码：先读 bounds 再按 inSampleSize 降采样，控制最大边长
    private fun decodeSampledBitmap(
        bytes: ByteArray,
        maxWidth: Int,
        maxHeight: Int,
    ): Bitmap? {
        // First pass: decode bounds only
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
        val (origW, origH) = opts.outWidth to opts.outHeight
        if (origW <= 0 || origH <= 0) return null

        // Calculate sample size (power of 2)
        var sampleSize = 1
        while (sampleSize * 2 <= maxOf(origW / maxWidth, origH / maxHeight)) {
            sampleSize *= 2
        }

        // Second pass: decode with sampling
        return BitmapFactory.Options().apply {
            inSampleSize = sampleSize
        }.let { BitmapFactory.decodeByteArray(bytes, 0, bytes.size, it) }
    }
}
