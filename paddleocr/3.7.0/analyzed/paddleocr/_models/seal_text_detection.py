# Copyright (c) 2025 PaddlePaddle Authors. All Rights Reserved.
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

# 印章文本检测：在印章区域定位可 OCR 的文字框
from .._utils.cli import (
    add_simple_inference_args,
    get_subcommand_args,
    perform_simple_inference,
)
from .base import PaddleXPredictorWrapper, PredictorCLISubcommandExecutor
from ._text_detection import TextDetectionMixin, TextDetectionSubcommandExecutorMixin


    # 默认 PP-OCRv4_mobile_seal_det，复用 TextDetectionMixin 后处理参数
class SealTextDetection(TextDetectionMixin, PaddleXPredictorWrapper):
    @property
        # 返回印章专用轻量检测模型名
    def default_model_name(self):
        return "PP-OCRv4_mobile_seal_det"

    @classmethod
    def get_cli_subcommand_executor(cls):
        return SealTextDetectionSubcommandExecutor()


    # seal_text_detection CLI：叠加通用推理与 DB 检测 flags
class SealTextDetectionSubcommandExecutor(
    TextDetectionSubcommandExecutorMixin, PredictorCLISubcommandExecutor
):
    @property
    def subparser_name(self):
        return "seal_text_detection"

    def _update_subparser(self, subparser):
        add_simple_inference_args(subparser)
        self._add_text_detection_args(subparser)

        # 收集 limit_side_len/thresh 等参数并执行推理
    def execute_with_args(self, args):
        params = get_subcommand_args(args)
        perform_simple_inference(SealTextDetection, params)
