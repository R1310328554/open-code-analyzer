"""
LLM 工具插件基类与 OpenAI function calling 元数据转换。

子类通过 pluginlib 注册，并由 PluginManager 按 name 索引。
"""

from typing import Any, TypedDict
import pluginlib

from .common import PLUGIN_TYPE_LLM_TOOLS


class LLMToolParameter(TypedDict):
    type: str
    description: str
    displayDescription: str
    required: bool


class LLMToolMetadata(TypedDict):
    name: str
    displayName: str
    description: str
    displayDescription: str
    parameters: dict[str, LLMToolParameter]


@pluginlib.Parent(PLUGIN_TYPE_LLM_TOOLS)
class LLMToolPlugin:
    """
    LLM 可调用工具插件抽象基类：子类实现 get_metadata 与 invoke。
    """
    @classmethod
    @pluginlib.abstractmethod
    def get_metadata(cls) -> LLMToolMetadata:
        pass

    def invoke(self, **kwargs) -> str:
        raise NotImplementedError


def llm_tool_metadata_to_openai_tool(llm_tool_metadata: LLMToolMetadata) -> dict[str, Any]:
    """将插件元数据转换为 OpenAI tools API 的 function 定义。"""
    """将插件元数据转换为 OpenAI tools API 的 function 定义。"""
    return {
        "type": "function",
        "function": {
            "name": llm_tool_metadata["name"],
            "description": llm_tool_metadata["description"],
            "parameters": {
                "type": "object",
                "properties": {k: {"type": p["type"], "description": p["description"]} for k, p in llm_tool_metadata["parameters"].items()},
                "required": [k for k, p in llm_tool_metadata["parameters"].items() if p["required"]],
            },
        },
    }
