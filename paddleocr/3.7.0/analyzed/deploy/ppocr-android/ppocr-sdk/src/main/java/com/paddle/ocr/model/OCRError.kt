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

package com.paddle.ocr.model

// PaddleOCR SDK 统一异常层次
package com.paddle.ocr.model

/**
 * OCR 错误密封类：模型、配置、图像与推理各阶段的 typed 异常。
 */
sealed class OCRError(message: String, cause: Throwable? = null) : Exception(message, cause) {
    // assets 中找不到指定 ONNX 或配置文件
    class ModelNotFound(modelPath: String, cause: Throwable? = null) : OCRError("Model not found: $modelPath", cause)
    // ONNX Session 创建或输入名解析失败
    class ModelLoadFailed(modelName: String, cause: Throwable) : OCRError("Failed to load $modelName model", cause)
    // inference.yml 缺少 PostProcess 或 character_dict
    class ConfigParseFailed(path: String, cause: Throwable? = null) : OCRError("Failed to parse config: $path", cause)
    // 输入 Bitmap 尺寸为零或字节流无法解码
    class InvalidImage : OCRError("Input image is empty or invalid")
    // det/rec ONNX 前向或张量创建失败
    class InferenceFailed(stage: String, cause: Throwable) : OCRError("Inference failed at stage '$stage'", cause)
    // CTC 或后处理解码逻辑错误
    class DecodeError(message: String, cause: Throwable? = null) : OCRError(message, cause)
}
