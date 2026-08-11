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

// YamlUtils.kt — 轻量 YAML 解析辅助（缩进层级判定）。

package com.paddle.ocr.utilpackage com.paddle.ocr.util

/**
 * YamlUtils 提供无第三方依赖的 YAML 行级解析辅助，
 * 用于读取 OCR 配置文件中的嵌套键值结构。
 */
object YamlUtils {

    // leadingSpaces 返回行首连续空格数，用于推断 YAML 嵌套深度。
    fun leadingSpaces(line: String): Int {
        return line.indexOfFirst { it != ' ' }.let { if (it < 0) line.length else it }
    }
}
