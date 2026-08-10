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
Agent 插件工具 API：列出 GlobalPluginManager 注册的全部 LLM 工具元数据。
"""

#


from quart import Response
from api.apps import login_required
from api.utils.api_utils import get_json_result
from agent.plugin import GlobalPluginManager


# GET /plugin/tools：返回内置插件工具的 metadata 列表
@manager.route("/plugin/tools", methods=["GET"])  # noqa: F821
@login_required
def llm_tools() -> Response:
    # 从全局插件管理器收集 LLM 可调用工具
    tools = GlobalPluginManager.get_llm_tools()
    tools_metadata = [t.get_metadata() for t in tools]

    return get_json_result(data=tools_metadata)
