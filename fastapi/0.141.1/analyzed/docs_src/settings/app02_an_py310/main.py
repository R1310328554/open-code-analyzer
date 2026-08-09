"""示例 02（Annotated）：get_settings + Depends 注入 Settings，lru_cache 保证进程内单例。"""

from functools import lru_cache
from typing import Annotated

from fastapi import Depends, FastAPI

from .config import Settings

app = FastAPI()  # 创建 FastAPI 应用实例


@lru_cache
def get_settings():
    """缓存 Settings 实例；测试可通过 app.dependency_overrides 替换此依赖。"""
    return Settings()


@app.get("/info")
async def info(settings: Annotated[Settings, Depends(get_settings)]):
    """Annotated[Settings, Depends(get_settings)] 声明配置依赖，便于类型检查。"""
    return {
        "app_name": settings.app_name,
        "admin_email": settings.admin_email,
        "items_per_user": settings.items_per_user,
    }
