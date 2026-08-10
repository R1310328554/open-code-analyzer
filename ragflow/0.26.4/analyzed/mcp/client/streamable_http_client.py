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
MCP Streamable HTTP 客户端示例：连接 RAGFlow MCP 服务并调用 ragflow_retrieval 工具。
"""


from mcp import ClientSession
from mcp.client.streamable_http import streamablehttp_client


async def main():
    # 演示 Streamable HTTP 传输下初始化会话、列举工具并发起检索
    try:
        # host 模式下需在请求头携带 api_key 或 OAuth Bearer Token 完成鉴权
        # async with streamablehttp_client("http://localhost:9382/mcp/", headers={"api_key": "ragflow-fixS-TicrohljzFkeLLWIaVhW7XlXPXIUW5solFor6o"}) as (read_stream, write_stream, _):
        # 亦可按 OAuth 2.1 第 5 节使用 Authorization: Bearer 头
        # async with streamablehttp_client("http://localhost:9382/mcp/", headers={"Authorization": "Bearer ragflow-fixS-TicrohljzFkeLLWIaVhW7XlXPXIUW5solFor6o"}) as (read_stream, write_stream, _):
        async with streamablehttp_client("http://localhost:9382/mcp/") as (read_stream, write_stream, _):
            async with ClientSession(read_stream, write_stream) as session:
                await session.initialize()
                tools = await session.list_tools()
                print(f"{tools.tools=}")
                response = await session.call_tool(name="ragflow_retrieval", arguments={"dataset_ids": ["bc4177924a7a11f09eff238aa5c10c94"], "document_ids": [], "question": "How to install neovim?"})
                print(f"Tool response: {response.model_dump()}")
    except Exception as e:
        print(e)


if __name__ == "__main__":
    from anyio import run

    run(main)
