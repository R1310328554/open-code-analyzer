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


"""MCP SSE 客户端示例：连接 RAGFlow 并调用 ragflow_retrieval 工具。"""

from mcp.client.session import ClientSession
from mcp.client.sse import sse_client


async def main():
    try:
        # host 模式下需在请求头附加 api_key 标识身份。
        # async with sse_client("http://localhost:9382/sse", headers={"api_key": "..."}) as streams:
        # 或使用 OAuth 2.1 Section 5 要求的 Authorization: Bearer 头。
        # async with sse_client(..., headers={"Authorization": "Bearer ..."}) as streams:

        async with sse_client("http://localhost:9382/sse") as streams:
            async with ClientSession(
                streams[0],
                streams[1],
            ) as session:
                await session.initialize()  # 初始化 MCP 会话
                tools = await session.list_tools()  # 列出可用工具
                print(f"{tools.tools=}")
                response = await session.call_tool(  # 调用 ragflow_retrieval 检索工具
                    name="ragflow_retrieval",
                    arguments={
                        "dataset_ids": ["ce3bb17cf27a11efa69751e139332ced"],
                        "document_ids": [],
                        "question": "How to install neovim?",
                    },
                )
                print(f"Tool response: {response.model_dump()}")

    except Exception as e:
        print(e)


if __name__ == "__main__":
    from anyio import run

    run(main)
# client.py — MCP SSE 客户端示例：连接 RAGFlow 服务器并调用 ragflow_retrieval 工具。
