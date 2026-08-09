"""教程 001（Annotated）：Annotated[FormData, Form()] 将表单字段解析为 Pydantic BaseModel。"""

from typing import Annotated

from fastapi import FastAPI, Form
from pydantic import BaseModel

app = FastAPI()  # 创建 FastAPI 应用实例


class FormData(BaseModel):
    """登录表单模型：username 与 password 对应 HTML 表单字段名。"""

    username: str
    password: str


@app.post("/login/")
async def login(data: Annotated[FormData, Form()]):
    """Form() 触发 multipart/form 或 urlencoded 解析；字段自动填充 FormData 实例。"""
    return data
