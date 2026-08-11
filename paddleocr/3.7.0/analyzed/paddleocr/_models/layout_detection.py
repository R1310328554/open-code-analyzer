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

# 版面区域检测：定位文档中标题、正文、表格、图片等版面块
from ._object_detection import (
    ObjectDetection,
    ObjectDetectionSubcommandExecutor,
)


    # 默认 PP-DocLayout_plus-L，继承 ObjectDetection 检测后处理
class LayoutDetection(ObjectDetection):
    @property
        # 返回 DocLayout 系列版面检测模型名
    def default_model_name(self):
        return "PP-DocLayout_plus-L"

    @classmethod
    def get_cli_subcommand_executor(cls):
        return LayoutDetectionSubcommandExecutor()


    # layout_detection CLI：注册 img_size/threshold 等检测超参
class LayoutDetectionSubcommandExecutor(ObjectDetectionSubcommandExecutor):
    @property
    def subparser_name(self):
        return "layout_detection"

    @property
        # 绑定 LayoutDetection 供子命令实例化
    def wrapper_cls(self):
        return LayoutDetection
