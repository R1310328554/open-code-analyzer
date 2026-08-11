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

# 表格识别参数：在 OCR 系统配置基础上追加表格结构模型
from deploy.hubserving.ocr_system.params import read_params as pp_ocr_read_params


# 继承 PP-OCR 参数并设置表格结构模型与字典
def read_params():
    cfg = pp_ocr_read_params()

    # 表格结构：最大边长、模型目录与结构字典路径
    # params for table structure model
    cfg.table_max_len = 488
    cfg.table_model_dir = "./inference/en_ppocr_mobile_v2.0_table_structure_infer/"
    cfg.table_char_dict_path = "./ppocr/utils/dict/table_structure_dict.txt"
    cfg.show_log = False
    return cfg
