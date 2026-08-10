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
Agent 画布模块：封装智能体 DSL、会话创建/列表/删除等 REST 调用。
"""



from .base import Base
from .session import Session


class Agent(Base):
    # RAGFlow 智能体（Canvas）资源：id、avatar、dsl 等字段
    def __init__(self, rag, res_dict):
        self.id = None
        self.avatar = None
        self.canvas_type = None
        self.description = None
        self.dsl = None
        super().__init__(rag, res_dict)

    class Dsl(Base):
        # 智能体 DSL 子对象：组件图、消息历史与引用
        def __init__(self, rag, res_dict):
            self.answer = []
            self.components = {"begin": {"downstream": ["Answer:China"], "obj": {"component_name": "Begin", "params": {}}, "upstream": []}}
            self.graph = {
                "edges": [],
                "nodes": [{"data": {"label": "Begin", "name": "begin"}, "id": "begin", "position": {"x": 50, "y": 200}, "sourcePosition": "left", "targetPosition": "right", "type": "beginNode"}],
            }
            self.history = []
            self.messages = []
            self.path = []
            self.reference = []
            super().__init__(rag, res_dict)

    def create_session(self, **kwargs) -> Session:
        # POST /agents/{id}/sessions 创建新会话
        res = self.post(f"/agents/{self.id}/sessions", json=kwargs)
        res = res.json()
        if res.get("code") == 0:
            return Session(self.rag, res.get("data"))
        raise Exception(res.get("message"))

    def list_sessions(self, page: int = 1, page_size: int = 30, orderby: str = "create_time", desc: bool = True, id: str = None) -> list[Session]:
        # GET 分页列出智能体会话
        res = self.get(f"/agents/{self.id}/sessions", {"page": page, "page_size": page_size, "orderby": orderby, "desc": desc, "id": id})
        res = res.json()
        if res.get("code") == 0:
            result_list = []
            for data in res.get("data"):
                temp_agent = Session(self.rag, data)
                result_list.append(temp_agent)
            return result_list
        raise Exception(res.get("message"))

    def delete_sessions(self, ids: list[str] | None = None, delete_all: bool = False):
        # DELETE 批量或全部删除会话
        payload = {"ids": ids}
        if delete_all:
            payload["delete_all"] = True
        res = self.rm(f"/agents/{self.id}/sessions", payload)
        res = res.json()
        if res.get("code") != 0:
            raise Exception(res.get("message"))
