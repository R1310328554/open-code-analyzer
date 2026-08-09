"""教程 003（Annotated）：Annotated 形式为多文件 File 参数附加 description 元数据。"""

from typing import Annotated

from fastapi import FastAPI, File, UploadFile
from fastapi.responses import HTMLResponse

app = FastAPI()  # 创建 FastAPI 应用实例


@app.post("/files/")
async def create_files(
    files: Annotated[list[bytes], File(description="Multiple files as bytes")],
):
    """Annotated 将类型、来源与文档描述绑定在同一声明中。"""
    return {"file_sizes": [len(file) for file in files]}


@app.post("/uploadfiles/")
async def create_upload_files(
    files: Annotated[
        list[UploadFile], File(description="Multiple files as UploadFile")
    ],
):
    """与 tutorial003 非 Annotated 版行为一致；适合复杂 File 元数据的集中写法。"""
    return {"filenames": [file.filename for file in files]}


@app.get("/")
async def main():
    """浏览器访问 / 可打开多文件上传测试表单。"""
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
