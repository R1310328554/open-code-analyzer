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

// PP-OCR 检测与识别超参数配置
package com.paddle.ocr

/**
 * PP-OCR 推理配置：DB 检测阈值、限边、unclip 与识别批大小等。
 */
data class PaddleOCRConfig(
    // 检测输入色彩空间：BGR 或 RGB
    val detImgMode: String = "BGR",
    // 检测缩放基准边长
    val detLimitSideLen: Int = 64,
    // 限边策略：min 或 max
    val detLimitType: String = "min",
    val detMaxSideLimit: Int = 4000,
    // DB 二值化像素阈值
    val detThresh: Float = 0.3f,
    // 文本框平均得分过滤阈值
    val detBoxThresh: Float = 0.6f,
    // 检测框扩展比例（unclip）
    val detUnclipRatio: Float = 1.5f,
    val detMaxCandidates: Int = 3000,
    val detUseDilation: Boolean = false,
    val detScoreMode: String = "fast",
    val detBoxType: String = "quad",
    // 识别置信度下限，低于此值的行丢弃
    val recScoreThresh: Float = 0.0f,
    // 识别批推理大小，1 时可记录逐行耗时
    val recBatchSize: Int = 1,
)
