"""教程 002：list[bytes] / list[UploadFile] 接收同一字段名的多文件上传，并提供 HTML 测试表单。"""

from fastapi import FastAPI, File, UploadFile
from fastapi.responses import HTMLResponse

app = FastAPI()  # 创建 FastAPI 应用实例


@app.post("/files/")
async def create_files(files: list[bytes] = File()):
    """HTML multiple 属性使浏览器提交多个文件；FastAPI 解析为 bytes 列表。"""
    return {"file_sizes": [len(file) for file in files]}


@app.post("/uploadfiles/")
async def create_upload_files(files: list[UploadFile]):
    """UploadFile 列表适合逐个流式处理大文件；此处汇总各文件名。"""
    return {"filenames": [file.filename for file in files]}


@app.get("/")
async def main():
    """返回含两个 multipart 表单的页面，便于在浏览器中手动测试多文件上传。"""
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
