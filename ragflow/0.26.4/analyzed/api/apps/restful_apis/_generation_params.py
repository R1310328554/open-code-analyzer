#
#  Copyright 2026 The InfiniFlow Authors. All Rights Reserved.
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
生成参数工具：从请求中提取 temperature/top_p 等并合并进 dialog.llm_setting。
"""

#

from copy import deepcopy

# OpenAI 风格生成参数键名
GENERATION_CONFIG_KEYS = ("temperature", "top_p", "frequency_penalty", "presence_penalty", "max_tokens")


def extract_generation_config(req):
    """从请求字典中提取非空的生成参数字段。"""
    return {key: req[key] for key in GENERATION_CONFIG_KEYS if key in req and req[key] is not None}


def pop_generation_config(req):
    """提取生成参数并从原请求中移除对应键。"""
    generation_config = extract_generation_config(req)
    for key in GENERATION_CONFIG_KEYS:
        req.pop(key, None)
    return generation_config


def merge_generation_config(dialog, generation_config):
    """将生成参数合并写入 dialog.llm_setting（深拷贝后 update）。"""
    if not generation_config:
        return
    llm_setting = deepcopy(getattr(dialog, "llm_setting", None) or {})
    llm_setting.update(generation_config)
    dialog.llm_setting = llm_setting
