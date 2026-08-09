"""教程 001：File() 与 Form() 混用——单 endpoint 接收文件与表单 token。"""

from fastapi import FastAPI, File, Form, UploadFile

app = FastAPI()  # 创建 FastAPI 应用实例


@app.post("/files/")
async def create_file(
    file: bytes = File(), fileb: UploadFile = File(), token: str = Form()
):
    """file 读入完整字节；fileb 保留 UploadFile 元数据；token 来自表单字段。"""
    return {
        "file_size": len(file),
        "token": token,
        "fileb_content_type": fileb.content_type,
    }
