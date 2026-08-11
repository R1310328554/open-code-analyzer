# Copyright (c) 2026 PaddlePaddle Authors. All Rights Reserved.
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

from typing import Any


# ocr/params.py — OCR 推理 runtime 参数白名单与默认值
# OCR_RUNTIME_PARAMS 定义 MCP 工具可接受的参数字段及 Python 类型
OCR_RUNTIME_PARAMS: dict[str, type] = {
    # use_doc_orientation_classify 是否启用文档方向分类
    "use_doc_orientation_classify": bool,
    # use_doc_unwarping 是否启用文档曲面矫正
    "use_doc_unwarping": bool,
    # use_textline_orientation 是否启用文本行方向校正
    "use_textline_orientation": bool,
    # text_det_limit_side_len 检测输入图像长边缩放上限
    "text_det_limit_side_len": int,
    "text_det_limit_type": str,
    # text_det_thresh 文本检测像素阈值
    "text_det_thresh": float,
    "text_det_box_thresh": float,
    "text_det_unclip_ratio": float,
    # text_rec_score_thresh 识别结果最低置信度过滤
    "text_rec_score_thresh": float,
}

# OCR_DEFAULT_PARAMS 未显式传入时的默认 runtime 参数
OCR_DEFAULT_PARAMS: dict[str, Any] = {
    "use_doc_orientation_classify": False,
    "use_doc_unwarping": False,
}
