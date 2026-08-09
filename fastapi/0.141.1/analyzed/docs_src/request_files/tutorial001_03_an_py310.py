"""教程 001-03（Annotated）：Annotated + File(description=...) 声明文件参数文档元数据。"""

from typing import Annotated

from fastapi import FastAPI, File, UploadFile

app = FastAPI()  # 创建 FastAPI 应用实例


@app.post("/files/")
async def create_file(file: Annotated[bytes, File(description="A file read as bytes")]):
    """Annotated 集中声明类型与 OpenAPI 描述；校验与解析行为不变。"""
    return {"file_size": len(file)}


@app.post("/uploadfile/")
async def create_upload_file(
    file: Annotated[UploadFile, File(description="A file read as UploadFile")],
):
    """UploadFile 也可包在 Annotated 内并附加 File(description=...)。"""
    return {"filename": file.filename}
