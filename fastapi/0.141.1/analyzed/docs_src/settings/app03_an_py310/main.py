"""app03 主模块（Annotated）：Depends + lru_cache 单例 Settings 依赖。"""

from functools import lru_cache
from typing import Annotated

from fastapi import Depends, FastAPI

from . import config

app = FastAPI()  # 创建 FastAPI 应用实例


@lru_cache
def get_settings():
    """缓存 Settings 实例；进程内只解析一次 .env/环境变量。"""
    return config.Settings()


@app.get("/info")
async def info(settings: Annotated[config.Settings, Depends(get_settings)]):
    """注入 Settings；Annotated 写法便于 IDE 类型提示与 OpenAPI 描述。"""
    return {
        "app_name": settings.app_name,
        "admin_email": settings.admin_email,
        "items_per_user": settings.items_per_user,
    }
