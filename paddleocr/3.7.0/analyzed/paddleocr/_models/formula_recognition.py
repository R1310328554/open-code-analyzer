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

# 公式识别模型：将数学公式图像转为 LaTeX 字符串
from .._utils.cli import (
    add_simple_inference_args,
    get_subcommand_args,
    perform_simple_inference,
)
from .base import PaddleXPredictorWrapper, PredictorCLISubcommandExecutor


    # 默认 PP-FormulaNet_plus-M，封装 PaddleX 公式识别 predictor
class FormulaRecognition(PaddleXPredictorWrapper):
    def __init__(
        self,
        *args,
        **kwargs,
    ):
        self._extra_init_args = {}
        super().__init__(*args, **kwargs)

    @property
    def default_model_name(self):
        return "PP-FormulaNet_plus-M"

    @classmethod
    def get_cli_subcommand_executor(cls):
        return FormulaRecognitionSubcommandExecutor()

        # 透传额外初始化参数至 create_predictor
    def _get_extra_paddlex_predictor_init_args(self):
        return self._extra_init_args


    # formula_recognition CLI：单张图像推理并输出 LaTeX
class FormulaRecognitionSubcommandExecutor(PredictorCLISubcommandExecutor):
    @property
    def subparser_name(self):
        return "formula_recognition"

    def _update_subparser(self, subparser):
        add_simple_inference_args(subparser)

        # 解析 CLI 参数并委托 perform_simple_inference 执行
    def execute_with_args(self, args):
        params = get_subcommand_args(args)
        perform_simple_inference(FormulaRecognition, params)
