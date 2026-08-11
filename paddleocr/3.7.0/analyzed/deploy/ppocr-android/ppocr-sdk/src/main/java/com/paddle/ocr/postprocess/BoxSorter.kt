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

// BoxSorter.kt — 将检测框按自然阅读顺序（自上而下、从左到右）排序。

package com.paddle.ocr.postprocesspackage com.paddle.ocr.postprocess

import com.paddle.ocr.model.OCRBox

/**
 * BoxSorter 对齐 PaddleX SortQuadBoxes 逻辑：
 * 先按左上角 y/x 排序，再在 10 像素行带内冒泡修正左右顺序。
 */
object BoxSorter {
    // ROW_THRESHOLD_Y 判定同一文本行的垂直距离阈值（像素）。
    private const val ROW_THRESHOLD_Y = 10f

    // sortInReadingOrder 返回按阅读顺序排列的检测框副本。
    fun sortInReadingOrder(boxes: List<OCRBox>): List<OCRBox> {
        if (boxes.size <= 1) return boxes

        val list = boxes.toMutableList()
        list.sortWith(compareBy({ it.points[0].y }, { it.points[0].x }))

        // 与 PaddleX SortQuadBoxes 一致：在 10px 行带内自左向右冒泡交换。
        // Align with PaddleX SortQuadBoxes: bubble left-to-right inside a 10px row band.
        for (i in 0 until list.size - 1) {
            var j = i
            while (j >= 0) {
                val next = list[j + 1]
                val curr = list[j]
                if (kotlin.math.abs(next.points[0].y - curr.points[0].y) < ROW_THRESHOLD_Y &&
                    next.points[0].x < curr.points[0].x
                ) {
                    list[j] = next
                    list[j + 1] = curr
                    j--
                } else {
                    break
                }
            }
        }
        return list
    }
}
