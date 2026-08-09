"""教程 008：StreamingResponse 通过同步生成器分块读取本地大文件。"""

from fastapi import FastAPI
from fastapi.responses import StreamingResponse

some_file_path = "large-video-file.mp4"
app = FastAPI()


@app.get("/")
def main():
    def iterfile():  # (1) 同步生成器，逐块读取文件
        with open(some_file_path, mode="rb") as file_like:  # (2) 二进制模式打开
            yield from file_like  # (3) 委托给文件对象迭代，避免一次性读入内存

    return StreamingResponse(iterfile(), media_type="video/mp4")
