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

# 文档结构系统参数：在表格 OCR 配置基础上追加 layout 与输出模式
from deploy.hubserving.structure_table.params import read_params as table_read_params


# 继承表格参数并设置 structure 模式与 layout 字典
def read_params():
    cfg = table_read_params()

    # 版面解析：模型目录（可为空由上游提供）与 PubLayNet 字典
    # params for layout parser model
    cfg.layout_model_dir = ""
    cfg.layout_dict_path = "./ppocr/utils/dict/layout_publaynet_dict.txt"
    cfg.layout_score_threshold = 0.5
    cfg.layout_nms_threshold = 0.5

    cfg.mode = "structure"
    cfg.output = "./output"
    return cfg
