"""教程 001（Annotated）：Annotated[str, Form()] 分别声明 username 与 password 表单字段。"""

from typing import Annotated

from fastapi import FastAPI, Form

app = FastAPI()  # 创建 FastAPI 应用实例


@app.post("/login/")
async def login(username: Annotated[str, Form()], password: Annotated[str, Form()]):
    """各字段独立声明为 Form；返回 username（示例未回传 password）。"""
    return {"username": username}
