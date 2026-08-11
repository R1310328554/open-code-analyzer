# Copyright (c) 2022 PaddlePaddle Authors. All Rights Reserved.
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

from __future__ import absolute_import
from __future__ import division
from __future__ import print_function


# params.py — OCR 文本检测服务默认参数（DB 算法阈值与模型路径）。

# Config 占位类，实际字段由 read_params 动态赋值。
class Config(object):
    pass


# read_params 返回 DB 检测器所需的算法名、模型目录与后处理超参。
def read_params():
    cfg = Config()

    # 检测算法、模型路径、边长限制及 DB 阈值/扩框比例等参数。
    # params for text detector
    cfg.det_algorithm = "DB"
    cfg.det_model_dir = "./inference/PP-OCRv3_mobile_det_infer/"
    cfg.det_limit_side_len = 960
    cfg.det_limit_type = "max"

    # Differentiable Binarization 后处理：阈值、框过滤、unclip 与评分模式。
    # DB params
    cfg.det_db_thresh = 0.3
    cfg.det_db_box_thresh = 0.6
    cfg.det_db_unclip_ratio = 1.5
    cfg.use_dilation = False
    cfg.det_db_score_mode = "fast"

    # #EAST params
    # cfg.det_east_score_thresh = 0.8
    # cfg.det_east_cover_thresh = 0.1
    # cfg.det_east_nms_thresh = 0.2

    cfg.use_pdserving = False
    cfg.use_tensorrt = False

    return cfg
