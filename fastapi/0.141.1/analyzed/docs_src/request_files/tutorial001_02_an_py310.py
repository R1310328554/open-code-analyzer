"""教程 001-02（Annotated）：Annotated[bytes | None, File()] 声明可选文件字节上传。"""

from typing import Annotated

from fastapi import FastAPI, File, UploadFile

app = FastAPI()  # 创建 FastAPI 应用实例


@app.post("/files/")
async def create_file(file: Annotated[bytes | None, File()] = None):
    """File() 元数据写在 Annotated 内；默认 None 表示文件字段可省略。"""
    if not file:
        return {"message": "No file sent"}
    else:
        return {"file_size": len(file)}


@app.post("/uploadfile/")
async def create_upload_file(file: UploadFile | None = None):
    """UploadFile 端点与 tutorial001_02 非 Annotated 版行为相同。"""
    if not file:
        return {"message": "No upload file sent"}
    else:
        return {"filename": file.filename}
