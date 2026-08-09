"""教程 001-02：可选文件上传——bytes | None = File(default=None) 与 UploadFile | None。"""

from fastapi import FastAPI, File, UploadFile

app = FastAPI()  # 创建 FastAPI 应用实例


@app.post("/files/")
async def create_file(file: bytes | None = File(default=None)):
    """multipart 未附带文件字段时 file 为 None；有文件时读入完整字节并返回长度。"""
    if not file:
        return {"message": "No file sent"}
    else:
        return {"file_size": len(file)}


@app.post("/uploadfile/")
async def create_upload_file(file: UploadFile | None = None):
    """UploadFile 流式处理大文件；未上传时返回提示，否则返回原始文件名。"""
    if not file:
        return {"message": "No upload file sent"}
    else:
        return {"filename": file.filename}
