"""
GitHub 连接器模型：序列化 Repository 以便写入检查点并在恢复时重建 PyGithub 对象。
"""
from typing import Any

from github import Repository
from github.Requester import Requester
from pydantic import BaseModel


class SerializedRepository(BaseModel):
    # raw_headers + raw_data 足够 Requester 还原 Repository
    # id is part of the raw_data as well, just pulled out for convenience
    id: int
    headers: dict[str, str | int]
    raw_data: dict[str, Any]

    def to_Repository(self, requester: Requester) -> Repository.Repository:
        # 从检查点缓存反序列化为 PyGithub Repository
        return Repository.Repository(requester, self.headers, self.raw_data, completed=True)
