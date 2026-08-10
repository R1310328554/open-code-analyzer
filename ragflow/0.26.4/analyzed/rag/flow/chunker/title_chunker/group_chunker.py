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
按标题层级分组的分块策略：同节内按 token 上下限合并相邻文本行。
"""

from common.token_utils import num_tokens_from_string
from rag.flow.chunker.title_chunker.common import (
    BaseTitleChunker,
    resolve_target_level,
)

MIN_GROUP_TOKENS = 32  # 分组最小 token 阈值
MAX_GROUP_TOKENS = 1024  # 分组最大 token 阈值


def _build_section_ids(levels, target_level):
    """为每行生成节 id：遇到不高于目标层级的标题则递增。"""
    sec_ids = []
    sid = 0
    for i, level in enumerate(levels):
        if target_level is not None and level <= target_level and i > 0:
            sid += 1
        sec_ids.append(sid)
    return sec_ids


def _resolve_group_target_level(levels, hierarchy, most_level):
    """解析分组目标层级：优先 hierarchy，否则取众数层级。"""
    if hierarchy and int(hierarchy) > 0:
        return resolve_target_level(levels, hierarchy)
    return most_level


class GroupTitleChunker(BaseTitleChunker):
    """标题分组分块器：同节内合并小 chunk，跨节受 token 上限约束。"""
    start_message = "Start to group by title levels."

    def resolve_levels(self, line_records):
        return self.resolve_title_levels(line_records)

    def build_chunks(self, line_records, resolved):
        target_level = _resolve_group_target_level(
            resolved["levels"],
            self.param.hierarchy,
            resolved["most_level"],
        )
        sec_ids = _build_section_ids(resolved["levels"], target_level)
        record_groups = []
        tk_cnt = 0
        last_sid = -2

        # 合并状态由（当前节 id，当前 token 数）驱动
        # A chunk stays open while records remain in the same logical section,
        # except that very small chunks are allowed to absorb the next record
        # regardless of section change.
        for record, sec_id in zip(line_records, sec_ids):
            if record["doc_type_kwd"] != "text":
                record_groups.append([record])
                tk_cnt = 0
                last_sid = -2
                continue

            text = record["text"]
            if not text.strip():
                continue

            token_count = num_tokens_from_string(text)
            should_merge = record_groups and record_groups[-1][0]["doc_type_kwd"] == "text" and (tk_cnt < MIN_GROUP_TOKENS or (tk_cnt < MAX_GROUP_TOKENS and sec_id == last_sid))

            if should_merge:
                record_groups[-1].append(record)
                tk_cnt += token_count
            else:
                record_groups.append([record])
                tk_cnt = token_count

            last_sid = sec_id

        return self.build_chunks_from_record_groups(record_groups)
