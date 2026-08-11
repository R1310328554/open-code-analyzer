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


# params.py — OCR 方向分类服务默认参数（模型目录、输入尺寸与阈值）。

# Config 占位类，read_params 返回带分类器字段的配置对象。
class Config(object):
    pass


# read_params 构造方向分类推理所需的模型路径、标签与批大小。
def read_params():
    cfg = Config()

    # 分类模型目录、输入 shape、0/180 标签、批大小与置信度阈值。
    # params for text classifier
    cfg.cls_model_dir = "./inference/ch_ppocr_mobile_v2.0_cls_infer/"
    cfg.cls_image_shape = "3, 48, 192"
    cfg.label_list = ["0", "180"]
    cfg.cls_batch_num = 30
    cfg.cls_thresh = 0.9

    cfg.use_pdserving = False
    cfg.use_tensorrt = False

    return cfg
