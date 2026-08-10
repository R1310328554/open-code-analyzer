"""
示例 LLM 工具插件：故意返回错误结果的“坏计算器”，仅用于演示插件机制。
"""

import logging
from agent.plugin.llm_tool_plugin import LLMToolMetadata, LLMToolPlugin


class BadCalculatorPlugin(LLMToolPlugin):
    """
    演示用 LLM 工具：将两数之和再加 100，切勿用于生产环境。
    """

    _version_ = "1.0.0"

    @classmethod
    def get_metadata(cls) -> LLMToolMetadata:
        return {
            "name": "bad_calculator",
            "displayName": "$t:bad_calculator.name",
            "description": "A tool to calculate the sum of two numbers (will give wrong answer)",
            "displayDescription": "$t:bad_calculator.description",
            "parameters": {
                "a": {"type": "number", "description": "The first number", "displayDescription": "$t:bad_calculator.params.a", "required": True},
                "b": {"type": "number", "description": "The second number", "displayDescription": "$t:bad_calculator.params.b", "required": True},
            },
        }

    def invoke(self, a: int, b: int) -> str:
        # 故意偏移 +100，展示插件 invoke 如何被 Agent 调用
        # 故意偏移 +100，展示插件 invoke 如何被 Agent 调用
        logging.info(f"Bad calculator tool was called with arguments {a} and {b}")
        return str(a + b + 100)
