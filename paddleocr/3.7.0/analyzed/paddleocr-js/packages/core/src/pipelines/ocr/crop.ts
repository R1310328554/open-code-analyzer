/*
 * Copyright (c) 2026 PaddlePaddle Authors. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

// 检测框裁剪：将任意四边形透视变换为水平文本条带，必要时旋转 90°
import type { OpenCv, Mat } from "@techstark/opencv-js";

import { getMiniBoxFromPoints } from "../../models/common";
import type { Point2D } from "../../models/common";

  // 输入原图 Mat 与四点 poly，输出 rectified 裁剪 Mat（调用方负责 delete）
export function cropByPoly(cv: OpenCv, srcMat: Mat, poly: Point2D[]): Mat {
  // 将 poly 排序为 mini box 四角，估算 crop 宽高
  const ordered = getMiniBoxFromPoints(cv, poly).box;
  const widthTop = Math.hypot(ordered[1][0] - ordered[0][0], ordered[1][1] - ordered[0][1]);
  const widthBottom = Math.hypot(ordered[2][0] - ordered[3][0], ordered[2][1] - ordered[3][1]);
  const heightLeft = Math.hypot(ordered[3][0] - ordered[0][0], ordered[3][1] - ordered[0][1]);
  const heightRight = Math.hypot(ordered[2][0] - ordered[1][0], ordered[2][1] - ordered[1][1]);
  const cropW = Math.max(1, Math.floor(Math.max(widthTop, widthBottom)));
  const cropH = Math.max(1, Math.floor(Math.max(heightLeft, heightRight)));

  const srcTri = cv.matFromArray(4, 1, cv.CV_32FC2, [
    ordered[0][0],
    ordered[0][1],
    ordered[1][0],
    ordered[1][1],
    ordered[2][0],
    ordered[2][1],
    ordered[3][0],
    ordered[3][1]
  ]);
  const dstTri = cv.matFromArray(4, 1, cv.CV_32FC2, [0, 0, cropW, 0, cropW, cropH, 0, cropH]);
  const transform = cv.getPerspectiveTransform(srcTri, dstTri);
  const warped = new cv.Mat();
  // 透视变换拉直文本行，BORDER_REPLICATE 填充边界
  cv.warpPerspective(
    srcMat,
    warped,
    transform,
    new cv.Size(cropW, cropH),
    cv.INTER_CUBIC,
    cv.BORDER_REPLICATE,
    new cv.Scalar()
  );
  srcTri.delete();
  dstTri.delete();
  transform.delete();

    // 竖长条带逆时针旋转 90°，适配识别模型横向输入
  if (warped.rows / Math.max(1, warped.cols) >= 1.5) {
    const rotated = new cv.Mat();
    cv.rotate(warped, rotated, cv.ROTATE_90_COUNTERCLOCKWISE);
    warped.delete();
    return rotated;
  }
  return warped;
}
