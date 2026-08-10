#
#  Copyright 2025 The InfiniFlow Authors. All Rights Reserved.
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
Agent 模块全局常量：浮点比较容差与参数嵌套深度上限。
"""

#

# 浮点相等比较的默认 epsilon
# 浮点相等比较的默认 epsilon
FLOAT_ZERO = 1e-8
# 组件参数 JSON 解析允许的最大嵌套深度
# 组件参数 JSON 解析允许的最大嵌套深度
PARAM_MAXDEPTH = 5
