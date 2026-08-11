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


# params.py — OCR 文本识别服务默认参数（CRNN 模型、字典与输入尺寸）。

# Config 占位类，read_params 返回完整识别配置对象。
class Config(object):
    pass


# read_params 设置识别算法、模型目录、字符表路径及批处理大小。
def read_params():
    cfg = Config()

    # CRNN 识别模型路径、输入 shape、最大文本长度与字符字典配置。
    # params for text recognizer
    cfg.rec_algorithm = "CRNN"
    cfg.rec_model_dir = "./inference/ch_PP-OCRv3_rec_infer/"

    cfg.rec_image_shape = "3, 48, 320"
    cfg.rec_batch_num = 6
    cfg.max_text_length = 25

    cfg.rec_char_dict_path = "./ppocr/utils/ppocr_keys_v1.txt"
    cfg.use_space_char = True

    cfg.use_pdserving = False
    cfg.use_tensorrt = False

    return cfg
