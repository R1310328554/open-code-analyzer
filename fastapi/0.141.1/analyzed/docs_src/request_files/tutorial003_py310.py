"""教程 003：多文件上传并为 File 字段设置 description，同时保留 HTML 测试页。"""

from fastapi import FastAPI, File, UploadFile
from fastapi.responses import HTMLResponse

app = FastAPI()  # 创建 FastAPI 应用实例


@app.post("/files/")
async def create_files(
    files: list[bytes] = File(description="Multiple files as bytes"),
):
    """description 帮助 API 使用者理解该字段接收多个 bytes 文件。"""
    return {"file_sizes": [len(file) for file in files]}


@app.post("/uploadfiles/")
async def create_upload_files(
    files: list[UploadFile] = File(description="Multiple files as UploadFile"),
):
    """UploadFile 列表同样可用 File(description=...) 标注 OpenAPI 说明。"""
    return {"filenames": [file.filename for file in files]}


@app.get("/")
async def main():
    """根路径返回双表单 HTML，分别 POST 到 /files/ 与 /uploadfiles/。"""
    content = """
<body>
<form action="/files/" enctype="multipart/form-data" method="post">
<input name="files" type="file" multiple>
<input type="submit">
</form>
<form action="/uploadfiles/" enctype="multipart/form-data" method="post">
<input name="files" type="file" multiple>
<input type="submit">
</form>
</body>
    """
    return HTMLResponse(content=content)
