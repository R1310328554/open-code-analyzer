# 应用配置子系统：导出 AppConfig 与全局 apps 注册表
from .config import AppConfig
from .registry import apps

__all__ = ["AppConfig", "apps"]
