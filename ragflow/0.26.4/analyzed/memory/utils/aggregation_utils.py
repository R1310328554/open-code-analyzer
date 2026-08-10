#
#  Copyright 2025 The InfiniFlow Authors. All Rights Reserved.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use it except in compliance with the License.
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

"""检索结果纯聚合辅助函数（无重型依赖）。"""


def aggregate_by_field(messages: list | None, field_name: str) -> list[tuple[str, int]]:
    # 统计指定字段取值频次，合并预聚合与逐条计数
    """按字段聚合消息文档，返回 [(值, 计数), ...]。

    支持已聚合行（含 "value"/"count"）与
    逐文档字段值（str 或 str 列表）。
    """
    if not messages:
        return []

    counts: dict[str, int] = {}
    result: list[tuple[str, int]] = []

    for doc in messages:
        if "value" in doc and "count" in doc:
            result.append((doc["value"], doc["count"]))
            continue

        if field_name not in doc:
            continue

        v = doc[field_name]
        if isinstance(v, list):
            for vv in v:
                if isinstance(vv, str):
                    key = vv.strip()
                    if key:
                        counts[key] = counts.get(key, 0) + 1
        elif isinstance(v, str):
            key = v.strip()
            if key:
                counts[key] = counts.get(key, 0) + 1

    if counts:
        for k, v in counts.items():
            result.append((k, v))

    return result
