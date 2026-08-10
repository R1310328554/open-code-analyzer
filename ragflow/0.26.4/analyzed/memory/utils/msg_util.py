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
#
"""
记忆 LLM 响应解析工具：从模型输出中提取 JSON 对象。
"""


import json


def get_json_result_from_llm_response(response_str: str) -> dict:
    """
    解析 LLM 响应字符串，提取 JSON 内容。
    去除 ```json 围栏后直接 json.loads；失败返回空字典。
    解析失败时返回 {}。

    :param response_str: LLM 原始响应文本。
    :return: 解析得到的字典对象。
    """
    try:
        clean_str = response_str.strip()
        if clean_str.startswith("```json"):
            clean_str = clean_str[7:]  # Remove the starting ```json
        if clean_str.endswith("```"):
            clean_str = clean_str[:-3]  # Remove the ending ```

        return json.loads(clean_str.strip())
    except (ValueError, json.JSONDecodeError):
        return {}
