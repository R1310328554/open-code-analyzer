"""
Agent 插件包入口：导出全局 PluginManager 单例 GlobalPluginManager。
"""

from .plugin_manager import PluginManager

GlobalPluginManager = PluginManager()
