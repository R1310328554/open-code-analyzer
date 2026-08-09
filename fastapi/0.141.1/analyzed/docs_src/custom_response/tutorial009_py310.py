"""教程 009：FileResponse 直接返回本地文件路径，由框架发送文件内容。"""

from fastapi import FastAPI
from fastapi.responses import FileResponse

some_file_path = "large-video-file.mp4"
app = FastAPI()


@app.get("/")
async def main():
    """FileResponse 自动设置 Content-Type 与 Content-Disposition 等头。"""
    return FileResponse(some_file_path)
