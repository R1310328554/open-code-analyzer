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
Agent 工具基类与 LLM 工具调用会话：定义 ToolMeta、参数解析与同步/异步 invoke。

LLMToolPluginCallSession 负责将 LLM function call 分派到 MCP 或本地工具实例。
"""

#
import logging
import re
import time
from copy import deepcopy
import asyncio
from functools import partial
from collections.abc import Mapping
from typing import TypedDict, List, Any
from agent.component.base import ComponentParamBase, ComponentBase
from common.misc_utils import hash_str2int
from rag.prompts.generator import kb_prompt
from common.mcp_tool_call_conn import MCPToolBinding, MCPToolCallSession, ToolCallSession
from timeit import default_timer as timer


from common.misc_utils import thread_pool_exec


class ToolParameter(TypedDict):
    """OpenAI function 单个参数的 schema 字段定义。"""
    type: str
    description: str
    displayDescription: str
    enum: List[str]
    required: bool


class ToolMeta(TypedDict):
    """工具元数据：名称、描述及 parameters 字典。"""
    name: str
    displayName: str
    description: str
    displayDescription: str
    parameters: dict[str, ToolParameter]


class LLMToolPluginCallSession(ToolCallSession):
    """
    LLM 插件工具调用会话：按名称从 tools_map 查找并同步/异步执行。
    """
    def __init__(self, tools_map: dict[str, object], callback: partial):
        self.tools_map = tools_map
        self.callback = callback

    def tool_call(self, name: str, arguments: dict[str, Any], timeout: float | int = 10) -> Any:
        # 同步入口：包装 asyncio.run 调用异步实现
        return asyncio.run(self.tool_call_async(name, arguments, request_timeout=timeout))

    async def tool_call_async(self, name: str, arguments: dict[str, Any], request_timeout: float | int = 10) -> Any:
        assert name in self.tools_map, f"LLM tool {name} does not exist"
        logging.info(f"[ToolCall] invoke name={name} arguments={str(arguments)[:200]}")
        if not isinstance(arguments, Mapping):
            raise TypeError(f"Tool arguments for {name} must be an object, got {type(arguments).__name__}")
        st = timer()
        tool_obj = self.tools_map[name]
        # 按工具类型分派：MCP 绑定、MCP 会话、原生 async 或线程池同步 invoke
        if isinstance(tool_obj, MCPToolBinding):
            resp = await thread_pool_exec(tool_obj.session.tool_call, tool_obj.original_name, arguments, request_timeout)
        elif isinstance(tool_obj, MCPToolCallSession):
            resp = await thread_pool_exec(tool_obj.tool_call, name, arguments, request_timeout)
        elif hasattr(tool_obj, "invoke_async") and asyncio.iscoroutinefunction(tool_obj.invoke_async):
            resp = await tool_obj.invoke_async(**arguments)
        else:
            resp = await thread_pool_exec(tool_obj.invoke, **arguments)

        if resp is None and hasattr(tool_obj, "output") and callable(tool_obj.output):
            try:
                fallback_output = tool_obj.output()
                if isinstance(fallback_output, dict) and fallback_output.get("content") not in (None, ""):
                    resp = fallback_output["content"]
                elif fallback_output not in (None, ""):
                    resp = fallback_output
                else:
                    resp = fallback_output
                logging.warning(
                    f"[ToolCall] resp is None, fallback to output name={name} output_keys={list(fallback_output.keys()) if isinstance(fallback_output, dict) else type(fallback_output).__name__}"
                )
            except Exception as e:
                logging.warning(f"[ToolCall] resp is None and output fallback failed name={name} err={e}")

        elapsed = timer() - st
        logging.info(f"[ToolCall] done name={name} elapsed={elapsed:.2f}s result={str(resp)[:200]}")
        self.callback(name, arguments, resp, elapsed_time=elapsed)
        return resp

    def get_tool_obj(self, name):
        return self.tools_map[name]


class ToolParamBase(ComponentParamBase):
    """
    工具参数基类：从 meta 初始化 inputs 与默认值，并导出 OpenAI function schema。
    """
    def __init__(self):
        # self.meta:ToolMeta = None
        super().__init__()
        self._init_inputs()
        self._init_attr_by_meta()

    def _init_inputs(self):
        # 深拷贝 meta.parameters 到 self.inputs
        self.inputs = {}
        for k, p in self.meta["parameters"].items():
            self.inputs[k] = deepcopy(p)

    def _init_attr_by_meta(self):
        for k, p in self.meta["parameters"].items():
            if not hasattr(self, k):
                setattr(self, k, p.get("default"))

    def get_meta(self):
        params = {}
        for k, p in self.meta["parameters"].items():
            params[k] = {"type": p["type"], "description": p["description"]}
            if "enum" in p:
                params[k]["enum"] = p["enum"]

        desc = getattr(self, "description", None) or self.meta["description"]
        function_name = getattr(self, "function_name", self.meta["name"])

        return {
            "type": "function",
            "function": {
                "name": function_name,
                "description": desc,
                "parameters": {"type": "object", "properties": params, "required": [k for k, p in self.meta["parameters"].items() if p["required"]]},
            },
        }


class ToolBase(ComponentBase):
    """
    画布工具组件基类：封装 invoke/invoke_async 与检索结果块 _retrieve_chunks。
    """
    def __init__(self, canvas, id, param: ComponentParamBase):
        from agent.canvas import Canvas  # Local import to avoid cyclic dependency

        assert isinstance(canvas, Canvas), "canvas must be an instance of Canvas"
        self._canvas = canvas
        self._id = id
        self._param = param
        self._param.check()

    def get_meta(self) -> dict[str, Any]:
        return self._param.get_meta()

    def invoke(self, **kwargs):
        # 记录耗时、调用 _invoke，异常写入 _ERROR 输出
        if self.check_if_canceled("Tool processing"):
            return

        self.set_output("_created_time", time.perf_counter())
        try:
            res = self._invoke(**kwargs)
        except Exception as e:
            self._param.outputs["_ERROR"] = {"value": str(e)}
            logging.exception(e)
            res = str(e)
        self._param.debug_inputs = []

        self.set_output("_elapsed_time", time.perf_counter() - self.output("_created_time"))
        return res

    async def invoke_async(self, **kwargs):
        """
        异步调用包装：优先 _invoke_async / 协程 _invoke，否则在线程池执行。
        """
        if self.check_if_canceled("Tool processing"):
            return

        self.set_output("_created_time", time.perf_counter())
        try:
            fn_async = getattr(self, "_invoke_async", None)
            if fn_async and asyncio.iscoroutinefunction(fn_async):
                res = await fn_async(**kwargs)
            elif asyncio.iscoroutinefunction(self._invoke):
                res = await self._invoke(**kwargs)
            else:
                res = await thread_pool_exec(self._invoke, **kwargs)
        except Exception as e:
            self._param.outputs["_ERROR"] = {"value": str(e)}
            logging.exception(e)
            res = str(e)
        self._param.debug_inputs = []

        self.set_output("_elapsed_time", time.perf_counter() - self.output("_created_time"))
        return res

    def _retrieve_chunks(self, res_list: list, get_title, get_url, get_content, get_score=None):
        # 将检索结果转为 chunk/aggs，写入画布引用并生成 kb_prompt 文本
        chunks = []
        aggs = []
        for r in res_list:
            content = get_content(r)
            if not content:
                continue
            # 剔除内嵌 base64 图片 markdown，并截断过长正文
            content = re.sub(r"!?\[[a-z]+\]\(data:image/png;base64,[ 0-9A-Za-z/_=+-]+\)", "", content)
            content = content[:10000]
            if not content:
                continue
            id = str(hash_str2int(content))
            title = get_title(r)
            url = get_url(r)
            score = get_score(r) if get_score else 1
            chunks.append({"chunk_id": id, "content": content, "doc_id": id, "docnm_kwd": title, "similarity": score, "url": url})
            aggs.append({"doc_name": title, "doc_id": id, "count": 1, "url": url})
        self._canvas.add_reference(chunks, aggs)
        self.set_output("formalized_content", "\n".join(kb_prompt({"chunks": chunks, "doc_aggs": aggs}, 200000, True)))

    def thoughts(self) -> str:
        return self._canvas.get_component_name(self._id) + " is running..."
