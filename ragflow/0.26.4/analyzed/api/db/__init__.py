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
api.db 包：Peewee ORM 枚举与租户/文件/流水线等领域常量定义。
"""

#

from enum import IntEnum
from enum import StrEnum

from common.constants import PipelineTaskType


class UserTenantRole(StrEnum):
    # 用户在租户内的角色
    OWNER = "owner"
    ADMIN = "admin"
    NORMAL = "normal"
    INVITE = "invite"


class TenantPermission(StrEnum):
    # 知识库/Canvas 等资源可见性：仅本人或团队
    ME = "me"
    TEAM = "team"


class SerializedType(IntEnum):
    PICKLE = 1
    JSON = 2


class FileType(StrEnum):
    # 文件管理器中的逻辑类型（含 folder/virtual）
    PDF = "pdf"
    DOC = "doc"
    VISUAL = "visual"
    AURAL = "aural"
    VIRTUAL = "virtual"
    FOLDER = "folder"
    OTHER = "other"


VALID_FILE_TYPES = {FileType.PDF, FileType.DOC, FileType.VISUAL, FileType.AURAL, FileType.VIRTUAL, FileType.FOLDER, FileType.OTHER}


class InputType(StrEnum):
    # 数据连接器输入模式
    LOAD_STATE = "load_state"  # e.g. loading a current full state or a save state, such as from a file
    POLL = "poll"  # e.g. calling an API to get all documents in the last hour
    EVENT = "event"  # e.g. registered an endpoint as a listener, and processing connector events
    SLIM_RETRIEVAL = "slim_retrieval"


class CanvasCategory(StrEnum):
    Agent = "agent_canvas"
    DataFlow = "dataflow_canvas"


VALID_PIPELINE_TASK_TYPES = {
    PipelineTaskType.PARSE,
    PipelineTaskType.DOWNLOAD,
    PipelineTaskType.RAPTOR,
    PipelineTaskType.GRAPH_RAG,
    PipelineTaskType.MINDMAP,
    PipelineTaskType.ARTIFACT,
    PipelineTaskType.SKILL,
}


# 知识库级扇出任务：Task.doc_id 使用占位符，collect_task 再回填真实 doc_id
# KB-level fan-out task types: their Task row uses GRAPH_RAPTOR_FAKE_DOC_ID as a
# sentinel doc_id, and ``task_executor.collect_task`` substitutes the first real
# doc_id from ``msg["doc_ids"]`` before re-running ``TaskService.get_task`` so
# the join through Document → Knowledgebase → Tenant resolves and tenant_id /
# kb_id / language are hydrated onto the task dict. Add new fan-out task types
# here or TaskContext will raise "Task must contain 'tenant_id'".
PIPELINE_SPECIAL_PROGRESS_FREEZE_TASK_TYPES = {
    PipelineTaskType.RAPTOR.lower(),
    PipelineTaskType.GRAPH_RAG.lower(),
    PipelineTaskType.MINDMAP.lower(),
    PipelineTaskType.ARTIFACT.lower(),
    PipelineTaskType.SKILL.lower(),
}


# 虚拟文件夹名：挂载知识库与 Skill 空间
KNOWLEDGEBASE_FOLDER_NAME = ".knowledgebase"
SKILLS_FOLDER_NAME = "skills"
