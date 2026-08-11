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

# 表格单元格检测：在有线表格图像中定位每个单元格边界框
from ._object_detection import (
    ObjectDetection,
    ObjectDetectionSubcommandExecutor,
)


    # 默认 RT-DETR-L_wired_table_cell_det，RT-DETR 单元格检测
class TableCellsDetection(ObjectDetection):
    @property
        # 返回有线表格单元格检测预置模型名
    def default_model_name(self):
        return "RT-DETR-L_wired_table_cell_det"

    @classmethod
    def get_cli_subcommand_executor(cls):
        return TableCellsDetectionSubcommandExecutor()


    # table_cells_detection CLI 子命令执行器
class TableCellsDetectionSubcommandExecutor(ObjectDetectionSubcommandExecutor):
    @property
    def subparser_name(self):
        return "table_cells_detection"

    @property
        # 绑定 TableCellsDetection 包装类
    def wrapper_cls(self):
        return TableCellsDetection
