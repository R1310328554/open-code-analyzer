"""教程 001（Annotated）：Annotated[bytes, File()] 声明 multipart 文件字节参数。"""

from typing import Annotated

from fastapi import FastAPI, File, UploadFile

app = FastAPI()  # 创建 FastAPI 应用实例


@app.post("/files/")
async def create_file(file: Annotated[bytes, File()]):
    """Annotated 将 File() 元数据绑定到类型；行为与 tutorial001 非 Annotated 版一致。"""
    return {"file_size": len(file)}


@app.post("/uploadfile/")
async def create_upload_file(file: UploadFile):
    """UploadFile 端点无需 Annotated；FastAPI 自动识别为文件上传参数。"""
    return {"filename": file.filename}
