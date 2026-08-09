"""教程 003：关闭 Swagger UI 深度链接（deepLinking）。"""

from fastapi import FastAPI

# deepLinking=False 时 URL 不会随当前操作变化
app = FastAPI(swagger_ui_parameters={"deepLinking": False})


@app.get("/users/{username}")
async def read_user(username: str):
    """示例路由；文档页禁用按操作锚定 URL。"""
    return {"message": f"Hello {username}"}
