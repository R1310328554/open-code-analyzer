"""教程 007：StreamingResponse 配合异步生成器逐块推送响应体。"""

import anyio
from fastapi import FastAPI
from fastapi.responses import StreamingResponse

app = FastAPI()


async def fake_video_streamer():
    """模拟视频流：循环 yield 字节块，anyio.sleep 让出事件循环。"""
    for i in range(10):
        yield b"some fake video bytes"
        await anyio.sleep(0)


@app.get("/")
async def main():
    """StreamingResponse 接受 async generator，适合大文件或实时流。"""
    return StreamingResponse(fake_video_streamer())
