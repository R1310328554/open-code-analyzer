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

# 通用文本检测：DB/EAST 等算法定位图像中的文字区域
from .._utils.cli import (
    add_simple_inference_args,
    get_subcommand_args,
    perform_simple_inference,
)
from .base import PaddleXPredictorWrapper, PredictorCLISubcommandExecutor
from ._text_detection import TextDetectionMixin, TextDetectionSubcommandExecutorMixin


    # 默认 PP-OCRv6_medium_det，Mixin 注入 limit_side_len 等后处理
class TextDetection(TextDetectionMixin, PaddleXPredictorWrapper):
    @property
        # 返回 PP-OCRv6 中等规模检测模型名
    def default_model_name(self):
        return "PP-OCRv6_medium_det"

    @classmethod
    def get_cli_subcommand_executor(cls):
        return TextDetectionSubcommandExecutor()


    # text_detection CLI：通用推理 + DB 检测专用 argparse 选项
class TextDetectionSubcommandExecutor(
    TextDetectionSubcommandExecutorMixin, PredictorCLISubcommandExecutor
):
    @property
    def subparser_name(self):
        return "text_detection"

    def _update_subparser(self, subparser):
        add_simple_inference_args(subparser)
        self._add_text_detection_args(subparser)

        # 收集检测超参并委托 perform_simple_inference
    def execute_with_args(self, args):
        params = get_subcommand_args(args)
        perform_simple_inference(TextDetection, params)
