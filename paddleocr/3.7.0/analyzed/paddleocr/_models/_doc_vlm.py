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

import abc

from .._utils.cli import (
    get_subcommand_args,
    perform_simple_inference,
)
from .base import PaddleXPredictorWrapper, PredictorCLISubcommandExecutor
# 文档 VLM 模型基类：多模态 dict 输入与 CLI 子命令执行器骨架
from paddlex.utils.pipeline_arguments import custom_type


    # 文档视觉语言模型包装：extra init 参数透传 create_predictor
class BaseDocVLM(PaddleXPredictorWrapper):
    def __init__(
        self,
        *args,
        **kwargs,
    ):
        self._extra_init_args = {}
        super().__init__(*args, **kwargs)

    def _get_extra_paddlex_predictor_init_args(self):
        return self._extra_init_args


    # VLM CLI 执行器：校验 dict 型 input 并调用 perform_simple_inference
class BaseDocVLMSubcommandExecutor(PredictorCLISubcommandExecutor):
    input_validator = staticmethod(custom_type(dict))

    @property
    @abc.abstractmethod
    def wrapper_cls(self):
        raise NotImplementedError

        # 解析子命令参数、校验 input 字典并启动推理
    def execute_with_args(self, args):
        params = get_subcommand_args(args)
        params["input"] = self.input_validator(params["input"])
        perform_simple_inference(self.wrapper_cls, params)
