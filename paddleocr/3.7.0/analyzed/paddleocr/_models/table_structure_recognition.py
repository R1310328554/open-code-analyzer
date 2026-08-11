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

# 表格结构识别 SLANet：预测表格 HTML/结构序列
from .._utils.cli import (
    add_simple_inference_args,
    get_subcommand_args,
    perform_simple_inference,
)
from .base import PaddleXPredictorWrapper, PredictorCLISubcommandExecutor


    # 默认 SLANet，将表格图像转为结构化 markup
class TableStructureRecognition(PaddleXPredictorWrapper):
    def __init__(
        self,
        *args,
        **kwargs,
    ):
        self._extra_init_args = {}
        super().__init__(*args, **kwargs)

    @property
    def default_model_name(self):
        return "SLANet"

    @classmethod
    def get_cli_subcommand_executor(cls):
        return TableStructureRecognitionSubcommandExecutor()

        # 透传额外 predictor 初始化参数
    def _get_extra_paddlex_predictor_init_args(self):
        return self._extra_init_args


    # table_structure_recognition CLI 子命令
class TableStructureRecognitionSubcommandExecutor(PredictorCLISubcommandExecutor):
    @property
    def subparser_name(self):
        return "table_structure_recognition"

    def _update_subparser(self, subparser):
        add_simple_inference_args(subparser)

        # 解析参数并执行单次表格结构推理
    def execute_with_args(self, args):
        params = get_subcommand_args(args)
        perform_simple_inference(TableStructureRecognition, params)
