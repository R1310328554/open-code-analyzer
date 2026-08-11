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

from .._utils.cli import add_simple_inference_args
# 图表解析模型 PP-Chart2Table：将图表图像转为结构化表格
from ._doc_vlm import (
    BaseDocVLM,
    BaseDocVLMSubcommandExecutor,
)


    # 默认模型 PP-Chart2Table，复用 BaseDocVLM 多模态推理链路
class ChartParsing(BaseDocVLM):
    @property
        # 返回 PaddleX 预置图表解析模型名称
    def default_model_name(self):
        return "PP-Chart2Table"

    @classmethod
    def get_cli_subcommand_executor(cls):
        return ChartParsingSubcommandExecutor()


    # chart_parsing 子命令：input 为含 image URL/路径 的 dict
class ChartParsingSubcommandExecutor(BaseDocVLMSubcommandExecutor):
    @property
    def subparser_name(self):
        return "chart_parsing"

    @property
    def wrapper_cls(self):
        return ChartParsing

        # 添加简单推理参数，input 示例为 JSON dict 字符串
    def _update_subparser(self, subparser):
        add_simple_inference_args(
            subparser,
            input_help='Input dict, e.g. `{"image": "https://paddle-model-ecology.bj.bcebos.com/paddlex/imgs/demo_image/chart_parsing_02.png"}`.',
        )
