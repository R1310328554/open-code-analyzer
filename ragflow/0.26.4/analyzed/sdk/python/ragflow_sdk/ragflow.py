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
RAGFlow Python SDK 主客户端：Bearer 认证 REST 封装，涵盖 Dataset/Chat/Agent/Memory/检索。
"""

from typing import Optional, Any

import requests

from .modules.agent import Agent
from .modules.chat import Chat
from .modules.chunk import Chunk
from .modules.dataset import DataSet
from .modules.memory import Memory


class RAGFlow:
    # SDK 入口：api_key + base_url，统一 HTTP 动词
    def __init__(self, api_key, base_url, version="v1"):
        """
        api_url: http://<host_address>/api/v1
        初始化 Bearer 认证与 API 根路径。
        """
        self.user_key = api_key
        self.api_url = f"{base_url}/api/{version}"
        self.authorization_header = {"Authorization": "{} {}".format("Bearer", self.user_key)}

    def post(self, path, json=None, stream=False, files=None):
        res = requests.post(url=self.api_url + path, json=json, headers=self.authorization_header, stream=stream, files=files)
        return res

    def get(self, path, params=None, json=None):
        res = requests.get(url=self.api_url + path, params=params, headers=self.authorization_header, json=json)
        return res

    def delete(self, path, json):
        res = requests.delete(url=self.api_url + path, json=json, headers=self.authorization_header)
        return res

    def put(self, path, json):
        res = requests.put(url=self.api_url + path, json=json, headers=self.authorization_header)
        return res

    def patch(self, path, json):
        res = requests.patch(url=self.api_url + path, json=json, headers=self.authorization_header)
        return res

    def create_dataset(
        # POST /datasets 创建知识库

        self,
        name: str,
        avatar: Optional[str] = None,
        description: Optional[str] = None,
        embedding_model: Optional[str] = None,
        permission: str = "me",
        chunk_method: str = "naive",
        parser_config: Optional[DataSet.ParserConfig] = None,
        auto_metadata_config: Optional[dict[str, Any]] = None,
    ) -> DataSet:
        payload = {
            "name": name,
            "avatar": avatar,
            "description": description,
            "embedding_model": embedding_model,
            "permission": permission,
            "chunk_method": chunk_method,
        }
        if parser_config is not None:
            payload["parser_config"] = parser_config.to_json()
        if auto_metadata_config is not None:
            payload["auto_metadata_config"] = auto_metadata_config

        res = self.post("/datasets", payload)
        res = res.json()
        if res.get("code") == 0:
            return DataSet(self, res["data"])
        raise Exception(res["message"])

    def delete_datasets(self, ids: list[str] | None = None, delete_all: bool = False):
        # 批量或全部删除知识库
        res = self.delete("/datasets", {"ids": ids, "delete_all": delete_all})
        res = res.json()
        if res.get("code") != 0:
            raise Exception(res["message"])

    def get_dataset(self, name: str):
        # 按名称查找首个匹配知识库
        _list = self.list_datasets(name=name)
        if len(_list) > 0:
            return _list[0]
        raise Exception("Dataset %s not found" % name)

    def list_datasets(self, page: int = 1, page_size: int = 30, orderby: str = "create_time", desc: bool = True, id: str | None = None, name: str | None = None) -> list[DataSet]:
        # 分页列出知识库
        res = self.get(
            "/datasets",
            {
                "page": page,
                "page_size": page_size,
                "orderby": orderby,
                "desc": desc,
                "id": id,
                "name": name,
            },
        )
        res = res.json()
        result_list = []
        if res.get("code") == 0:
            for data in res["data"]:
                result_list.append(DataSet(self, data))
            return result_list
        raise Exception(res["message"])

    def create_chat(
        # POST /chats 创建 RAG 对话助手

        self,
        name: str,
        icon: str = "",
        dataset_ids: list[str] | None = None,
        llm_id: str | None = None,
        llm_setting: dict | None = None,
        prompt_config: dict | None = None,
        **kwargs,
    ) -> Chat:
        payload = {"name": name, "icon": icon, "dataset_ids": dataset_ids or []}
        if llm_id is not None:
            payload["llm_id"] = llm_id
        if llm_setting is not None:
            payload["llm_setting"] = llm_setting
        if prompt_config is not None:
            payload["prompt_config"] = prompt_config
        payload.update(kwargs)
        res = self.post("/chats", payload)
        res = res.json()
        if res.get("code") == 0:
            return Chat(self, res["data"])
        raise Exception(res["message"])

    def delete_chats(self, ids: list[str] | None = None, delete_all: bool = False):
        res = self.delete("/chats", {"ids": ids, "delete_all": delete_all})
        res = res.json()
        if res.get("code") != 0:
            raise Exception(res["message"])

    def get_chat(self, chat_id: str) -> Chat:
        res = self.get(f"/chats/{chat_id}")
        res = res.json()
        if res.get("code") == 0:
            return Chat(self, res["data"])
        raise Exception(res["message"])

    def list_chats(
        self,
        page: int = 1,
        page_size: int = 30,
        orderby: str = "create_time",
        desc: bool = True,
        id: str | None = None,
        name: str | None = None,
        keywords: str | None = None,
        owner_ids: str | list[str] | None = None,
    ) -> list[Chat]:
        res = self.get(
            "/chats",
            {
                "page": page,
                "page_size": page_size,
                "orderby": orderby,
                "desc": desc,
                "id": id,
                "name": name,
                "keywords": keywords,
                "owner_ids": owner_ids,
            },
        )
        res = res.json()
        result_list = []
        if res.get("code") == 0:
            for data in res["data"]["chats"]:
                result_list.append(Chat(self, data))
            return result_list
        raise Exception(res["message"])

    def retrieve(
        # POST /retrieval 跨知识库向量+关键词混合检索

        self,
        dataset_ids,
        document_ids=None,
        question="",
        page=1,
        page_size=30,
        similarity_threshold=0.2,
        vector_similarity_weight=0.3,
        top_k=1024,
        rerank_id: str | None = None,
        keyword: bool = False,
        cross_languages: list[str] | None = None,
        metadata_condition: dict | None = None,
        use_kg: bool = False,
        toc_enhance: bool = False,
    ):
        if document_ids is None:
            document_ids = []
        data_json = {
            "page": page,
            "page_size": page_size,
            "similarity_threshold": similarity_threshold,
            "vector_similarity_weight": vector_similarity_weight,
            "top_k": top_k,
            "rerank_id": rerank_id,
            "keyword": keyword,
            "question": question,
            "dataset_ids": dataset_ids,
            "document_ids": document_ids,
            "cross_languages": cross_languages,
            "metadata_condition": metadata_condition,
            "use_kg": use_kg,
            "toc_enhance": toc_enhance,
        }
        # 构造检索 JSON 并 POST /retrieval
        res = self.post("/retrieval", json=data_json)
        res = res.json()
        if res.get("code") == 0:
            chunks = []
            for chunk_data in res["data"].get("chunks"):
                chunk = Chunk(self, chunk_data)
                chunks.append(chunk)
            return chunks
        raise Exception(res.get("message"))

    def list_agents(self, page: int = 1, page_size: int = 30, orderby: str = "update_time", desc: bool = True) -> list[Agent]:
        # GET /agents 列出 Canvas 智能体
        res = self.get(
            "/agents",
            {
                "page": page,
                "page_size": page_size,
                "orderby": orderby,
                "desc": desc,
            },
        )
        res = res.json()
        result_list = []
        if res.get("code") == 0:
            data = res.get("data") or {}
            data_list = data.get("canvas", [])
            for data in data_list:
                result_list.append(Agent(self, data))
            return result_list
        raise Exception(res["message"])

    def get_agent(self, agent_id: str) -> Agent:
        res = self.get(f"/agents/{agent_id}")
        res = res.json()
        if res.get("code") == 0:
            return Agent(self, res["data"])
        raise Exception(res["message"])

    def create_agent(
        # POST /agents 创建智能体（title + dsl）

        self,
        title: str,
        dsl: dict,
        description: str | None = None,
        canvas_type: str | None = None,
    ) -> None:
        req = {"title": title, "dsl": dsl}

        if description is not None:
            req["description"] = description

        if canvas_type is not None:
            req["canvas_type"] = canvas_type

        res = self.post("/agents", req)
        res = res.json()

        if res.get("code") != 0:
            raise Exception(res["message"])

    def update_agent(
        # PUT 更新智能体 title/description/dsl

        self,
        agent_id: str,
        title: str | None = None,
        description: str | None = None,
        dsl: dict | None = None,
        canvas_type: str | None = None,
    ) -> None:
        req = {}

        if title is not None:
            req["title"] = title

        if description is not None:
            req["description"] = description

        if dsl is not None:
            req["dsl"] = dsl

        if canvas_type is not None:
            req["canvas_type"] = canvas_type

        res = self.put(f"/agents/{agent_id}", req)
        res = res.json()

        if res.get("code") != 0:
            raise Exception(res["message"])

    def delete_agent(self, agent_id: str) -> None:
        # DELETE 删除智能体
        res = self.delete(f"/agents/{agent_id}", {})
        res = res.json()

        if res.get("code") != 0:
            raise Exception(res["message"])

    def create_memory(self, name: str, memory_type: list[str], embd_id: str, llm_id: str):
        # POST 创建长期记忆库
        payload = {"name": name, "memory_type": memory_type, "embd_id": embd_id, "llm_id": llm_id}
        res = self.post("/memories", payload)
        res = res.json()
        if res.get("code") != 0:
            raise Exception(res["message"])
        return Memory(self, res["data"])

    def list_memory(self, page: int = 1, page_size: int = 50, tenant_id: str | list[str] = None, memory_type: str | list[str] = None, storage_type: str = None, keywords: str = None) -> dict:
        # GET 分页列出记忆库
        res = self.get(
            "/memories",
            {
                "page": page,
                "page_size": page_size,
                "tenant_id": tenant_id,
                "memory_type": memory_type,
                "storage_type": storage_type,
                "keywords": keywords,
            },
        )
        res = res.json()
        if res.get("code") != 0:
            raise Exception(res["message"])
        result_list = []
        for data in res["data"]["memory_list"]:
            result_list.append(Memory(self, data))
        return {"code": res.get("code", 0), "message": res.get("message"), "memory_list": result_list, "total_count": res["data"]["total_count"]}

    def delete_memory(self, memory_id: str):
        res = self.delete(f"/memories/{memory_id}", {})
        res = res.json()
        if res.get("code") != 0:
            raise Exception(res["message"])

    def add_message(self, memory_id: list[str], agent_id: str, session_id: str, user_input: str, agent_response: str, user_id: str = "") -> str:
        # POST 写入对话到记忆；API Key 模式可传 user_id
        """向记忆库追加消息；user_id 仅在 API Key 认证时作为外部主体标识。"""
        payload = {"memory_id": memory_id, "agent_id": agent_id, "session_id": session_id, "user_input": user_input, "agent_response": agent_response, "user_id": user_id}
        res = self.post("/messages", payload)
        res = res.json()
        if res.get("code") != 0:
            raise Exception(res["message"])
        return res["message"]

    def search_message(
        # GET /messages/search 语义搜索记忆

        self,
        query: str,
        memory_id: list[str],
        agent_id: str = None,
        session_id: str = None,
        user_id: str = None,
        similarity_threshold: float = 0.2,
        keywords_similarity_weight: float = 0.7,
        top_n: int = 10,
    ) -> list[dict]:
        params = {
            "query": query,
            "memory_id": memory_id,
            "agent_id": agent_id,
            "session_id": session_id,
            "user_id": user_id,
            "similarity_threshold": similarity_threshold,
            "keywords_similarity_weight": keywords_similarity_weight,
            "top_n": top_n,
        }
        res = self.get("/messages/search", params)
        res = res.json()
        if res.get("code") != 0:
            raise Exception(res["message"])
        return res["data"]

    def get_recent_messages(self, memory_id: list[str], agent_id: str = None, session_id: str = None, limit: int = 10) -> list[dict]:
        # GET 最近 N 条记忆消息
        params = {"memory_id": memory_id, "agent_id": agent_id, "session_id": session_id, "limit": limit}
        res = self.get("/messages", params)
        res = res.json()
        if res.get("code") != 0:
            raise Exception(res["message"])
        return res["data"]
