"""教程 002（Annotated）：Annotated[FormData, Form()] + extra=forbid 拒绝未知表单字段。"""

from typing import Annotated

from fastapi import FastAPI, Form
from pydantic import BaseModel

app = FastAPI()  # 创建 FastAPI 应用实例


class FormData(BaseModel):
    """仅允许 username/password；额外字段触发 422。"""

    username: str
    password: str
    model_config = {"extra": "forbid"}


@app.post("/login/")
async def login(data: Annotated[FormData, Form()]):
    """Annotated 将 Form 元数据与模型类型绑定；校验行为与 tutorial002 一致。"""
    return data
