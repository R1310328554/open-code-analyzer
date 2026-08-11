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

// RecPreprocessor.kt — 识别模型批量预处理：定高缩放、归一化与宽度对齐。

package com.paddle.ocr.preprocesspackage com.paddle.ocr.preprocess

import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import kotlin.math.ceil

/**
 * RecPreprocessResult 识别批量预处理输出：NCHW float 张量及 shape。
 */
data class RecPreprocessResult(
    val tensorData: FloatArray,
    val shape: LongArray,
)

/**
 * RecPreprocessor 将文本行裁剪 Mat 列表打包为识别网络 batch 输入。
 * 固定高度 48、等比缩放、右侧零填充至 batch 内最大宽度。
 */
object RecPreprocessor {
    // FIXED_HEIGHT 识别模型输入固定高度（像素）。
    private const val FIXED_HEIGHT = 48
    // MAX_IMG_W 单张裁剪图最大宽度上限，防止极端长图。
    private const val MAX_IMG_W = 3200

    // preprocessBatch 批量预处理裁剪图，返回对齐宽度的 float 张量。
    fun preprocessBatch(crops: List<Mat>): RecPreprocessResult {
        // 第一步：BGR→RGB，定高等比缩放到 FIXED_HEIGHT。
        // Convert BGR to RGB and resize to fixed height while preserving aspect ratio
        val resizedMats = mutableListOf<Mat>()
        for (crop in crops) {
            // Convert BGR to RGB (model expects RGB input)
            val rgb = Mat()
            Imgproc.cvtColor(crop, rgb, Imgproc.COLOR_BGR2RGB)
            val h = rgb.rows()
            val w = rgb.cols()
            val aspectRatio = if (h > 0) w.toDouble() / h else 1.0
            val newW = ceil(FIXED_HEIGHT * aspectRatio).toInt().coerceAtMost(MAX_IMG_W)
            val dst = Mat()
            Imgproc.resize(rgb, dst, Size(newW.toDouble(), FIXED_HEIGHT.toDouble()), 0.0, 0.0, Imgproc.INTER_LINEAR)
            rgb.release()
            resizedMats.add(dst)
        }

        // 第二步：转 float 并按 (x/127.5)-1 归一化到 [-1,1]。
        // Convert to float and normalize: (x / 255 - 0.5) / 0.5 = x / 127.5 - 1
        val floatMats = mutableListOf<Mat>()
        for (mat in resizedMats) {
            val floatMat = Mat(mat.rows(), mat.cols(), CvType.CV_32FC3)
            mat.convertTo(floatMat, CvType.CV_32F)
            // Use Scalar with all 3 channels set — single-value Scalar only sets val[0]!
            Core.divide(floatMat, org.opencv.core.Scalar(127.5, 127.5, 127.5), floatMat)
            Core.subtract(floatMat, org.opencv.core.Scalar(1.0, 1.0, 1.0), floatMat)

            floatMats.add(floatMat)
            mat.release()  // Release resized mat
        }
        resizedMats.clear()

        val maxW = floatMats.maxOf { it.cols() }
        val n = floatMats.size

        // 第三步：右侧零填充，使 batch 内各样本宽度一致。
        // Pad to max width
        val paddedMats = mutableListOf<Mat>()
        for (mat in floatMats) {
            if (mat.cols() == maxW) {
                paddedMats.add(mat)
            } else {
                val padded = Mat(FIXED_HEIGHT, maxW, CvType.CV_32FC3, org.opencv.core.Scalar(0.0))
                val roi = padded.submat(0, FIXED_HEIGHT, 0, mat.cols())
                mat.copyTo(roi)
                roi.release()
                mat.release()
                paddedMats.add(padded)
            }
        }
        floatMats.clear()

        // 第四步：按 NCHW 布局拆分通道并写入连续 float 数组。
        // Build tensor data
        val channelSize = FIXED_HEIGHT * maxW
        val tensorData = FloatArray(n * 3 * channelSize)
        for (b in 0 until n) {
            val mat = paddedMats[b]
            val channels = mutableListOf<Mat>()
            Core.split(mat, channels)
            for (c in 0..2) {
                val buf = FloatArray(channelSize)
                channels[c].get(0, 0, buf)
                System.arraycopy(buf, 0, tensorData, (b * 3 + c) * channelSize, channelSize)
                channels[c].release()
            }
            mat.release()
        }
        paddedMats.clear()

        return RecPreprocessResult(
            tensorData = tensorData,
            shape = longArrayOf(n.toLong(), 3, FIXED_HEIGHT.toLong(), maxW.toLong()),
        )
    }
}
