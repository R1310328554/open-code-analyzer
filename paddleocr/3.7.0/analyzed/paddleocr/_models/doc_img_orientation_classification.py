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

# 文档图像方向分类：判断扫描页 0/90/180/270 度旋转并校正
from ._image_classification import (
    ImageClassification,
    ImageClassificationSubcommandExecutor,
)


    # 默认 PP-LCNet_x1_0_doc_ori，继承 ImageClassification 推理链路
class DocImgOrientationClassification(ImageClassification):
    @property
        # 返回 PaddleX 预置文档方向分类模型名称
    def default_model_name(self):
        return "PP-LCNet_x1_0_doc_ori"

    @classmethod
    def get_cli_subcommand_executor(cls):
        return DocImgOrientationClassificationSubcommandExecutor()


    # doc_img_orientation_classification CLI 子命令执行器
class DocImgOrientationClassificationSubcommandExecutor(
    ImageClassificationSubcommandExecutor
):
    @property
        # argparse 子命令名 doc_img_orientation_classification
    def subparser_name(self):
        return "doc_img_orientation_classification"

    @property
        # 绑定 DocImgOrientationClassification 包装类供 CLI 实例化
    def wrapper_cls(self):
        return DocImgOrientationClassification
