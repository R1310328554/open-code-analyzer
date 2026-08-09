"""示例 01 主应用：直接导入 config.settings 单例，/info 返回当前配置快照。"""

from fastapi import FastAPI

from .config import settings

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/info")
async def info():
    """读取模块级 settings；启动时已从环境变量加载，此处无 Depends 注入。"""
    return {
        "app_name": settings.app_name,
        "admin_email": settings.admin_email,
        "items_per_user": settings.items_per_user,
    }
