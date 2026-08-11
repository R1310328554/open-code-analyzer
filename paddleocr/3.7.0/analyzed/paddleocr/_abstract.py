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

# CLI 子命令执行器抽象基类：统一 add_subparser 与 execute_with_args 接口
import abc


    # 各 paddleocr 子命令（ocr、doc_parser 等）需实现的注册与执行契约
class CLISubcommandExecutor(metaclass=abc.ABCMeta):
    @abc.abstractmethod
        # 向 argparse 子解析器树注册本子命令的参数与 help
    def add_subparser(self, subparsers):
        raise NotImplementedError

    @abc.abstractmethod
        # 解析完成后根据 Namespace 执行具体业务逻辑
    def execute_with_args(self, args):
        raise NotImplementedError
