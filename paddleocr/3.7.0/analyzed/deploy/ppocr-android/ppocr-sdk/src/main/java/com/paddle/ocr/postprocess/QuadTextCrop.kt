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

// QuadTextCrop.kt — 按检测四边形做透视裁剪，得到水平文本行图像。

package com.paddle.ocr.postprocesspackage com.paddle.ocr.postprocess

import com.paddle.ocr.model.OCRBox
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import kotlin.math.hypot
import kotlin.math.max

/**
 * QuadTextCrop 对齐 PaddleX CropByPolys.get_minarea_rect_crop：
 * 重算最小外接矩形后透视变换；竖长条自动逆时针旋转 90°。
 */
object QuadTextCrop {
    // VERTICAL_CROP_RATIO 高宽比超过此值时判定为竖排文本并旋转。
    private const val VERTICAL_CROP_RATIO = 1.5

    // crop 从原图 Mat 中裁剪 box 对应文本区域，返回 BGR 子图。
    fun crop(src: Mat, box: OCRBox): Mat {
        // 与 PaddleX 一致：透视变换前基于检测四边形重算 minAreaRect。
        // Align with PaddleX CropByPolys.get_minarea_rect_crop: recompute minAreaRect
        // from the detected quad before perspective transform.
        val rectPoints = box.points.map { Point(it.x.toDouble(), it.y.toDouble()) }
        val rectInput = MatOfPoint2f()
        rectInput.fromList(rectPoints)
        val boundingBox = Imgproc.minAreaRect(rectInput)
        rectInput.release()

        val boxPoints = Array(4) { Point() }
        boundingBox.points(boxPoints)
        val ordered = QuadGeometry.orderMinAreaRectPoints(boxPoints)

        val widthTop = hypot(ordered[0].x - ordered[1].x, ordered[0].y - ordered[1].y)
        val widthBottom = hypot(ordered[2].x - ordered[3].x, ordered[2].y - ordered[3].y)
        val heightLeft = hypot(ordered[0].x - ordered[3].x, ordered[0].y - ordered[3].y)
        val heightRight = hypot(ordered[1].x - ordered[2].x, ordered[1].y - ordered[2].y)

        val dstW = max(widthTop, widthBottom).toInt().coerceAtLeast(1)
        val dstH = max(heightLeft, heightRight).toInt().coerceAtLeast(1)

        val srcPts = MatOfPoint2f()
        srcPts.fromList(ordered)
        val dstPts = MatOfPoint2f()
        dstPts.fromList(
            listOf(
                Point(0.0, 0.0),
                Point(dstW.toDouble(), 0.0),
                Point(dstW.toDouble(), dstH.toDouble()),
                Point(0.0, dstH.toDouble()),
            )
        )
        val m = Imgproc.getPerspectiveTransform(srcPts, dstPts)
        srcPts.release()
        dstPts.release()

        val dst = Mat(dstH, dstW, CvType.CV_8UC3)
        Imgproc.warpPerspective(
            src,
            dst,
            m,
            Size(dstW.toDouble(), dstH.toDouble()),
            Imgproc.INTER_CUBIC,
            Core.BORDER_REPLICATE,
        )
        m.release()

        if (dst.rows().toDouble() / dst.cols() >= VERTICAL_CROP_RATIO) {
            val rotated = Mat()
            Core.rotate(dst, rotated, Core.ROTATE_90_COUNTERCLOCKWISE)
            dst.release()
            return rotated
        }
        return dst
    }
}
