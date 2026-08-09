"""教程 002：为 Swagger UI 指定语法高亮主题（obsidian）。"""

from fastapi import FastAPI

# 将 syntaxHighlight 设为对象以选择主题
app = FastAPI(swagger_ui_parameters={"syntaxHighlight": {"theme": "obsidian"}})


@app.get("/users/{username}")
async def read_user(username: str):
    """示例路由；Swagger UI 使用 obsidian 高亮主题。"""
    return {"message": f"Hello {username}"}
