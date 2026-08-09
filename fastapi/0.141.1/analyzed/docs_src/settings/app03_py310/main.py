"""app03 主模块：Depends(get_settings) 注入配置，与 app03_an 逻辑相同。"""

from functools import lru_cache

from fastapi import Depends, FastAPI

from . import config

app = FastAPI()  # 创建 FastAPI 应用实例


@lru_cache
def get_settings():
    """缓存 Settings 实例；进程内只解析一次 .env/环境变量。"""
    return config.Settings()


@app.get("/info")
async def info(settings: config.Settings = Depends(get_settings)):
    """经典 Depends 写法；settings 类型为 config.Settings。"""
    return {
        "app_name": settings.app_name,
        "admin_email": settings.admin_email,
        "items_per_user": settings.items_per_user,
    }
