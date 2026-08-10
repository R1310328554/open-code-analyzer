#
#  Copyright 2024 The InfiniFlow Authors. All Rights Reserved.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

"""
任务执行器共享常量：打破 task_executor 与 refactor 模块间的循环导入。
Shared constants for task executor modules.

This module exists to break circular imports between task_executor.py and
task_executor_refactor modules.
"""

CANVAS_DEBUG_DOC_ID = "dataflow_x"  # Dataflow 画布调试用的假文档 ID
GRAPH_RAPTOR_FAKE_DOC_ID = "graph_raptor_x"  # GraphRAPTOR / 数据集级 RAPTOR 占位 doc_id
