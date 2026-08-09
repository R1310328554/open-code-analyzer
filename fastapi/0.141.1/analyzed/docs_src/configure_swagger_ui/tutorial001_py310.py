"""教程 001：关闭 Swagger UI 的语法高亮（syntaxHighlight）。"""

from fastapi import FastAPI

# swagger_ui_parameters 传入 Swagger UI 前端配置
app = FastAPI(swagger_ui_parameters={"syntaxHighlight": False})


@app.get("/users/{username}")
async def read_user(username: str):
    """示例路由；文档页将不启用代码语法高亮。"""
    return {"message": f"Hello {username}"}
