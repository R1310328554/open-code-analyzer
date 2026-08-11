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

# 表格类型分类：区分有线表格、无线表格等表格形态
from ._image_classification import (
    ImageClassification,
    ImageClassificationSubcommandExecutor,
)


    # 默认 PP-LCNet_x1_0_table_cls，继承 top-k 分类推理
class TableClassification(ImageClassification):
    @property
        # 返回表格分类 LCNet 预置模型名
    def default_model_name(self):
        return "PP-LCNet_x1_0_table_cls"

    @classmethod
    def get_cli_subcommand_executor(cls):
        return TableClassificationSubcommandExecutor()


    # table_classification CLI 子命令
class TableClassificationSubcommandExecutor(ImageClassificationSubcommandExecutor):
    @property
    def subparser_name(self):
        return "table_classification"

    @property
        # 绑定 TableClassification 供 CLI 调用
    def wrapper_cls(self):
        return TableClassification
