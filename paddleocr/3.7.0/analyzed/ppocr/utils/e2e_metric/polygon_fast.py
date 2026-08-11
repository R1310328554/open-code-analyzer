# Copyright (c) 2021 PaddlePaddle Authors. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# 多边形几何快算：Shapely 求面积、交并比与 IoD
import numpy as np
from shapely.geometry import Polygon

"""
:param det_x: [1, N] Xs of detection's vertices
:param det_y: [1, N] Ys of detection's vertices
:param gt_x: [1, N] Xs of groundtruth's vertices
:param gt_y: [1, N] Ys of groundtruth's vertices

##############
All the calculation of 'AREA' in this script is handled by:
1) First generating a binary mask with the polygon area filled up with 1's
2) Summing up all the 1's
"""


    # 由顶点坐标构造 Polygon 并返回面积
def area(x, y):
    polygon = Polygon(np.stack([x, y], axis=1))
    return float(polygon.area)


    # 近似交集：最小外接矩形相交面积
def approx_area_of_intersection(det_x, det_y, gt_x, gt_y):
    """
    This helper determine if both polygons are intersecting with each others with an approximation method.
    Area of intersection represented by the minimum bounding rectangular [xmin, ymin, xmax, ymax]
    """
    det_ymax = np.max(det_y)
    det_xmax = np.max(det_x)
    det_ymin = np.min(det_y)
    det_xmin = np.min(det_x)

    gt_ymax = np.max(gt_y)
    gt_xmax = np.max(gt_x)
    gt_ymin = np.min(gt_y)
    gt_xmin = np.min(gt_x)

    all_min_ymax = np.minimum(det_ymax, gt_ymax)
    all_max_ymin = np.maximum(det_ymin, gt_ymin)

    intersect_heights = np.maximum(0.0, (all_min_ymax - all_max_ymin))

    all_min_xmax = np.minimum(det_xmax, gt_xmax)
    all_max_xmin = np.maximum(det_xmin, gt_xmin)
    intersect_widths = np.maximum(0.0, (all_min_xmax - all_max_xmin))

    return intersect_heights * intersect_widths


    # 精确交集面积（buffer(0) 修复自交）
def area_of_intersection(det_x, det_y, gt_x, gt_y):
    p1 = Polygon(np.stack([det_x, det_y], axis=1)).buffer(0)
    p2 = Polygon(np.stack([gt_x, gt_y], axis=1)).buffer(0)
    return float(p1.intersection(p2).area)


def area_of_union(det_x, det_y, gt_x, gt_y):
    p1 = Polygon(np.stack([det_x, det_y], axis=1)).buffer(0)
    p2 = Polygon(np.stack([gt_x, gt_y], axis=1)).buffer(0)
    return float(p1.union(p2).area)


    # 标准 IoU = 交集 / 并集
def iou(det_x, det_y, gt_x, gt_y):
    return area_of_intersection(det_x, det_y, gt_x, gt_y) / (
        area_of_union(det_x, det_y, gt_x, gt_y) + 1.0
    )


    # IoD = 交集 / 检测框面积（Deteval 过滤用）
def iod(det_x, det_y, gt_x, gt_y):
    """
    This helper determine the fraction of intersection area over detection area
    """
    return area_of_intersection(det_x, det_y, gt_x, gt_y) / (area(det_x, det_y) + 1.0)
