"""示例 02 主应用：Depends(get_settings) 经典写法，lru_cache 缓存配置实例。"""

from functools import lru_cache

from fastapi import Depends, FastAPI

from .config import Settings

app = FastAPI()  # 创建 FastAPI 应用实例


@lru_cache
def get_settings():
    """首次调用时解析环境变量并缓存；后续请求复用同一 Settings 对象。"""
    return Settings()


@app.get("/info")
async def info(settings: Settings = Depends(get_settings)):
    """settings: Settings = Depends(get_settings) 等价于 Annotated 写法。"""
    return {
        "app_name": settings.app_name,
        "admin_email": settings.admin_email,
        "items_per_user": settings.items_per_user,
    }
