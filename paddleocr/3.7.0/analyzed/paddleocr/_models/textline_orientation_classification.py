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

# 文本行方向分类：判断单行文字 0/180 度朝向以便识别前旋转
from ._image_classification import (
    ImageClassification,
    ImageClassificationSubcommandExecutor,
)


    # 默认 PP-LCNet_x0_25_textline_ori 轻量方向分类
class TextLineOrientationClassification(ImageClassification):
    @property
        # 返回文本行方向 LCNet 预置模型名
    def default_model_name(self):
        return "PP-LCNet_x0_25_textline_ori"

    @classmethod
    def get_cli_subcommand_executor(cls):
        return TextLineOrientationClassificationSubcommandExecutor()


    # textline_orientation_classification CLI 子命令
class TextLineOrientationClassificationSubcommandExecutor(
    ImageClassificationSubcommandExecutor
):
    @property
    def subparser_name(self):
        return "textline_orientation_classification"

    @property
        # 绑定 TextLineOrientationClassification 包装类
    def wrapper_cls(self):
        return TextLineOrientationClassification
