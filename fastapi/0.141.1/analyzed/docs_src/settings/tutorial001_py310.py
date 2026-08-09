"""教程 001：最简 Settings——模块级单例，启动时一次性加载环境变量。"""

from fastapi import FastAPI
from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    """应用配置；admin_email 必填，app_name/items_per_user 有默认值。"""

    app_name: str = "Awesome API"
    admin_email: str
    items_per_user: int = 50


settings = Settings()  # 导入时即解析环境变量
app = FastAPI()


@app.get("/info")
async def info():
    """直接读取模块级 settings；适合小型应用，测试时需 monkeypatch 环境变量。"""
    return {
        "app_name": settings.app_name,
        "admin_email": settings.admin_email,
        "items_per_user": settings.items_per_user,
    }
