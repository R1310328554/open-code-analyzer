"""教程 001：Pydantic BaseModel + Form()——将表单字段绑定到模型并校验。"""

from fastapi import FastAPI, Form
from pydantic import BaseModel

app = FastAPI()  # 创建 FastAPI 应用实例


class FormData(BaseModel):
    """登录表单模型：username 与 password 须为字符串。"""

    username: str
    password: str


@app.post("/login/")
async def login(data: FormData = Form()):
    """application/x-www-form-urlencoded 或 multipart 表单解析为 FormData 并返回。"""
    return data
