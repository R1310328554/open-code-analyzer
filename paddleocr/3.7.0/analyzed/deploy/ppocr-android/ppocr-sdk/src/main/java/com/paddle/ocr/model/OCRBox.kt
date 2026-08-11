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

// OCR 检测框模型：四顶点四边形，用于绘制与透视裁剪
import android.graphics.PointF

/**
 * 文本检测框：由 4 个 PointF 顶点定义的凸四边形（通常为顺时针）。
 */
data class OCRBox(
    val points: List<PointF>,
) {
    // 构造时校验必须恰好 4 个点
    init {
        require(points.size == 4) { "OCRBox must have exactly 4 points, got ${points.size}" }
    }
}
