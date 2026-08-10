"""
RAG 提示词模块入口：从 generator 子模块 re-export 全部公开 API。
"""

from . import generator

__all__ = [name for name in dir(generator) if not name.startswith("_")]

globals().update({name: getattr(generator, name) for name in __all__})
