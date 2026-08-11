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

// MathUtils.kt — 数值舍入辅助，与 Python round half to even 行为对齐。

package com.paddle.ocr.utilpackage com.paddle.ocr.util

import kotlin.math.floor

/**
 * MathUtils 提供银行家舍入（round half to even），
 * 用于检测缩放与坐标映射时与 PaddleX 数值结果一致。
 */
object MathUtils {

    // roundHalfToEven 对 .5 边界取最近偶整数，避免累计偏差。
    fun roundHalfToEven(value: Double): Int {
        val floored = floor(value)
        val diff = value - floored
        return when {
            diff < 0.5 -> floored.toInt()
            diff > 0.5 -> floored.toInt() + 1
            floored.toInt() % 2 == 0 -> floored.toInt()
            else -> floored.toInt() + 1
        }
    }
}
