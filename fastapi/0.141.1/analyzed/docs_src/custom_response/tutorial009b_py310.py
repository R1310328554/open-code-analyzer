"""教程 009b：response_class=FileResponse，路由返回文件路径字符串。"""

from fastapi import FastAPI
from fastapi.responses import FileResponse

some_file_path = "large-video-file.mp4"
app = FastAPI()


@app.get("/", response_class=FileResponse)
async def main():
    """返回路径字符串；FastAPI 用 FileResponse 打开并流式发送文件。"""
    return some_file_path
