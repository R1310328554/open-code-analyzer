"""教程 002（Annotated）：Annotated[list[bytes], File()] 声明多文件字节上传参数。"""

from typing import Annotated

from fastapi import FastAPI, File, UploadFile
from fastapi.responses import HTMLResponse

app = FastAPI()  # 创建 FastAPI 应用实例


@app.post("/files/")
async def create_files(files: Annotated[list[bytes], File()]):
    """Annotated 写法；多值文件解析与 tutorial002 非 Annotated 版等价。"""
    return {"file_sizes": [len(file) for file in files]}


@app.post("/uploadfiles/")
async def create_upload_files(files: list[UploadFile]):
    """UploadFile 列表端点保持常规类型注解即可。"""
    return {"filenames": [file.filename for file in files]}


@app.get("/")
async def main():
    """提供 HTML 表单页面，演示 bytes 与 UploadFile 两种多文件上传路径。"""
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
