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

from deploy.hubserving.ocr_system.params import read_params as pp_ocr_read_params


# params.py — KIE SER 服务默认推理参数（模型路径、算法与可视化配置）。

# Config 占位类，实际配置由 read_params 返回的动态对象承载。
class Config(object):
    pass


# read_params 基于 OCR 系统参数扩展 SER 专用字段并返回 cfg。
def read_params():
    cfg = pp_ocr_read_params()

    # SER 算法与模型路径、类别字典及 OCR 文本排序方式。
    # SER params
    cfg.kie_algorithm = "LayoutXLM"
    cfg.use_visual_backbone = False

    cfg.ser_model_dir = "./inference/ser_vi_layoutxlm_xfund_infer"
    cfg.ser_dict_path = "train_data/XFUND/class_list_xfun.txt"
    cfg.vis_font_path = "./doc/fonts/simfang.ttf"
    cfg.ocr_order_method = "tb-yx"

    return cfg
