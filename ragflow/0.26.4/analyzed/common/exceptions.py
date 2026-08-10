# 通用业务异常：任务取消、参数错误、资源未找到与模型调用失败。
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


class TaskCanceledException(Exception):
    # 异步任务被用户或系统取消
    def __init__(self, msg):
        self.msg = msg


class ArgumentException(Exception):
    # 请求参数不合法
    def __init__(self, msg):
        self.msg = msg


class NotFoundException(Exception):
    # 目标资源不存在
    def __init__(self, msg):
        self.msg = msg


class ModelException(Exception):
    # LLM/嵌入模型调用失败，retryable 标记是否可重试
    def __init__(self, msg, retryable=False):
        super().__init__(msg)
        self.msg = msg
        self.retryable = retryable
