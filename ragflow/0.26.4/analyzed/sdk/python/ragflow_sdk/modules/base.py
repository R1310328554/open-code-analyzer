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
SDK 基类：从 API 响应字典递归构建对象，并代理 RAGFlow 客户端 HTTP 方法。
"""




class Base:
    # 所有 SDK 资源对象的公共基类
    def __init__(self, rag, res_dict):
        self.rag = rag
        self._update_from_dict(rag, res_dict)

    def _update_from_dict(self, rag, res_dict):
        # 递归将 dict 转为嵌套 Base 或原始字段
        for k, v in res_dict.items():
            if isinstance(v, dict):
                self.__dict__[k] = Base(rag, v)
            else:
                self.__dict__[k] = v

    def to_json(self):
        # 序列化为可 JSON 化的 dict（排除 rag 与私有属性）
        pr = {}
        for name in dir(self):
            value = getattr(self, name)
            if not name.startswith("__") and not callable(value) and name != "rag":
                if isinstance(value, Base):
                    pr[name] = value.to_json()
                else:
                    pr[name] = value
        return pr

    def post(self, path, json=None, stream=False, files=None):
        # 代理 RAGFlow.post
        res = self.rag.post(path, json, stream=stream, files=files)
        return res

    def get(self, path, params=None):
        # 代理 RAGFlow.get
        res = self.rag.get(path, params)
        return res

    def rm(self, path, json):
        # 代理 RAGFlow.delete
        res = self.rag.delete(path, json)
        return res

    def put(self, path, json):
        res = self.rag.put(path, json)
        return res

    def patch(self, path, json):
        res = self.rag.patch(path, json)
        return res

    def __str__(self):
        return str(self.to_json())
