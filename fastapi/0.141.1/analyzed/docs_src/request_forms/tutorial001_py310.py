"""教程 001：str = Form() 将 username/password 声明为必填表单字段。"""

from fastapi import FastAPI, Form

app = FastAPI()  # 创建 FastAPI 应用实例


@app.post("/login/")
async def login(username: str = Form(), password: str = Form()):
    """multipart 或 urlencoded 表单提交；缺字段时 FastAPI 返回 422。"""
    return {"username": username}
