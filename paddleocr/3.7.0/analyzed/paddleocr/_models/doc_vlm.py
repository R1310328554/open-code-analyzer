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

# 文档视觉语言模型 PP-DocBee2：图像+自然语言 query 多模态理解
from .._utils.cli import add_simple_inference_args
from ._doc_vlm import (
    BaseDocVLM,
    BaseDocVLMSubcommandExecutor,
)


    # 默认 PP-DocBee2-3B，复用 BaseDocVLM 的 dict 输入推理
class DocVLM(BaseDocVLM):
    @property
        # 返回 DocBee2 系列 VLM 预置模型名
    def default_model_name(self):
        return "PP-DocBee2-3B"

    @classmethod
    def get_cli_subcommand_executor(cls):
        return DocVLMSubcommandExecutor()


    # doc_vlm 子命令：input 为含 image 与 query 的 JSON dict
class DocVLMSubcommandExecutor(BaseDocVLMSubcommandExecutor):
    @property
    def subparser_name(self):
        return "doc_vlm"

    @property
    def wrapper_cls(self):
        return DocVLM

        # 注册简单推理参数，input 示例含 image URL 与 query 文本
    def _update_subparser(self, subparser):
        add_simple_inference_args(
            subparser,
            input_help='Input dict, e.g. `{"image": "https://paddle-model-ecology.bj.bcebos.com/paddlex/imgs/demo_image/medal_table.png", "query": "Recognize this table"}`.',
        )
