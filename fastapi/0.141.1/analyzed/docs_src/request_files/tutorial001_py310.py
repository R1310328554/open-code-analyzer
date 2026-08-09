"""教程 001：File() 读取字节与 UploadFile 流式接收 multipart 上传文件。"""

from fastapi import FastAPI, File, UploadFile

app = FastAPI()  # 创建 FastAPI 应用实例


@app.post("/files/")
async def create_file(file: bytes = File()):
    """File() 将整文件读入内存；返回字节长度供客户端确认上传大小。"""
    return {"file_size": len(file)}


@app.post("/uploadfile/")
async def create_upload_file(file: UploadFile):
    """UploadFile 适合大文件；此处仅返回客户端提供的原始文件名。"""
    return {"filename": file.filename}
