"""
插件管理器：扫描 embedded_plugins 目录，加载并索引 LLM 工具插件。
"""

import logging
import os
from pathlib import Path
import pluginlib

from .common import PLUGIN_TYPE_LLM_TOOLS

from .llm_tool_plugin import LLMToolPlugin


class PluginManager:
    """
    维护 name -> LLMToolPlugin 实例映射，供 Agent 按工具名批量解析。
    """

    _llm_tool_plugins: dict[str, LLMToolPlugin]

    def __init__(self) -> None:
        self._llm_tool_plugins = {}

    def load_plugins(self) -> None:
        # 通过 pluginlib 扫描 embedded_plugins 并注册 llm_tools 类型
        # 通过 pluginlib 扫描 embedded_plugins 并注册 llm_tools 类型
        loader = pluginlib.PluginLoader(paths=[str(Path(os.path.dirname(__file__), "embedded_plugins"))])

        for type, plugins in loader.plugins.items():
            for name, plugin in plugins.items():
                logging.info(f"Loaded {type} plugin {name} version {plugin.version}")

                if type == PLUGIN_TYPE_LLM_TOOLS:
                    metadata = plugin.get_metadata()
                    self._llm_tool_plugins[metadata["name"]] = plugin

    def get_llm_tools(self) -> list[LLMToolPlugin]:
        return list(self._llm_tool_plugins.values())

    def get_llm_tool_by_name(self, name: str) -> LLMToolPlugin | None:
        return self._llm_tool_plugins.get(name)

    def get_llm_tools_by_names(self, tool_names: list[str]) -> list[LLMToolPlugin]:
        results = []

        for name in tool_names:
            plugin = self._llm_tool_plugins.get(name)

            if plugin is not None:
                results.append(plugin)

        return results
