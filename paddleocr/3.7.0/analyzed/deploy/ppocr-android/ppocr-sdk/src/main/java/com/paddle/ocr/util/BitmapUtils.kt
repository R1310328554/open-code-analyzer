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

// BitmapUtils.kt — Android Bitmap 与 OpenCV Mat 之间的编解码与色彩空间转换。

package com.paddle.ocr.utilpackage com.paddle.ocr.util

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfByte
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc

/**
 * BitmapUtils 封装 imdecode、Bitmap↔Mat 转换，统一 BGR/RGBA 色彩约定。
 */
object BitmapUtils {

    // imdecodeBGR 从 JPEG/PNG 字节流解码为 BGR Mat。
    fun imdecodeBGR(imageBytes: ByteArray): Mat {
        val encoded = MatOfByte(*imageBytes)
        return try {
            Imgcodecs.imdecode(encoded, Imgcodecs.IMREAD_COLOR)
        } finally {
            encoded.release()
        }
    }

    // bitmapToBGRMat 将 ARGB Bitmap 转为 OpenCV BGR Mat。
    fun bitmapToBGRMat(bitmap: Bitmap): Mat {
        return bitmapToMat(bitmap, Imgproc.COLOR_RGBA2BGR)
    }

    // bitmapToRGBMat 将 ARGB Bitmap 转为 OpenCV RGB Mat。
    fun bitmapToRGBMat(bitmap: Bitmap): Mat {
        return bitmapToMat(bitmap, Imgproc.COLOR_RGBA2RGB)
    }

    // bgrMatToBitmap 将 BGR Mat 转回 ARGB_8888 Bitmap。
    fun bgrMatToBitmap(mat: Mat): Bitmap {
        val rgba = Mat()
        return try {
            Imgproc.cvtColor(mat, rgba, Imgproc.COLOR_BGR2RGBA)
            Bitmap.createBitmap(rgba.cols(), rgba.rows(), Bitmap.Config.ARGB_8888).also { bitmap ->
                Utils.matToBitmap(rgba, bitmap)
            }
        } finally {
            rgba.release()
        }
    }

    // bitmapToMat 内部通用转换：Bitmap→RGBA Mat→目标色彩空间。
    private fun bitmapToMat(bitmap: Bitmap, colorConversionCode: Int): Mat {
        val bmp = bitmap.copy(Bitmap.Config.ARGB_8888, false)
        val rgba = Mat(bmp.height, bmp.width, CvType.CV_8UC4)
        val dst = Mat()
        return try {
            Utils.bitmapToMat(bmp, rgba)
            Imgproc.cvtColor(rgba, dst, colorConversionCode)
            dst
        } catch (t: Throwable) {
            dst.release()
            throw t
        } finally {
            bmp.recycle()
            rgba.release()
        }
    }
}
