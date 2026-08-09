"""教程 001-03：File(description=...) 为 OpenAPI/Swagger 文档补充文件字段说明。"""

from fastapi import FastAPI, File, UploadFile

app = FastAPI()  # 创建 FastAPI 应用实例


@app.post("/files/")
async def create_file(file: bytes = File(description="A file read as bytes")):
    """description 仅影响 API 文档展示；运行时仍将文件完整读入 bytes。"""
    return {"file_size": len(file)}


@app.post("/uploadfile/")
async def create_upload_file(
    file: UploadFile = File(description="A file read as UploadFile"),
):
    """UploadFile 同样可通过 File(description=...) 标注文档说明文字。"""
    return {"filename": file.filename}
