"""教程 001（Annotated）：Annotated 同时声明 bytes/UploadFile 文件与 str Form 令牌。"""

from typing import Annotated

from fastapi import FastAPI, File, Form, UploadFile

app = FastAPI()  # 创建 FastAPI 应用实例


@app.post("/files/")
async def create_file(
    file: Annotated[bytes, File()],
    fileb: Annotated[UploadFile, File()],
    token: Annotated[str, Form()],
):
    """multipart 请求可同时携带文件字节、UploadFile 与 token 表单字段。"""
    return {
        "file_size": len(file),
        "token": token,
        "fileb_content_type": fileb.content_type,
    }
