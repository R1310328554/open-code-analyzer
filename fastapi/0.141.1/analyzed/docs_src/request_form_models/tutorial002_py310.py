"""教程 002：FormData 模型设置 extra=forbid，禁止未声明的额外表单键。"""

from fastapi import FastAPI, Form
from pydantic import BaseModel

app = FastAPI()  # 创建 FastAPI 应用实例


class FormData(BaseModel):
    """model_config extra=forbid 时多传字段会返回 422 Unprocessable Entity。"""

    username: str
    password: str
    model_config = {"extra": "forbid"}


@app.post("/login/")
async def login(data: FormData = Form()):
    """整表绑定到 Pydantic 模型；仅 username 与 password 被接受。"""
    return data
