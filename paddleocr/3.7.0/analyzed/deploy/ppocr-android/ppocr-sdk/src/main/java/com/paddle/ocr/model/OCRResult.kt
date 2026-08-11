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

// 单行 OCR 识别结果数据模型
package com.paddle.ocr.model

/**
 * 单条识别结果：检测框、识别文本、置信度及可选字级框。
 */
data class OCRResult(
    // 该行文本对应的四边形检测框
    val box: OCRBox,
    // CTC 解码后的字符串
    val text: String,
    // 识别平均置信度，用于 recScoreThresh 过滤
    val confidence: Float,
    // 可选字级/词级子框，默认可空
    val wordBoxes: List<OCRBox>? = null,
)
