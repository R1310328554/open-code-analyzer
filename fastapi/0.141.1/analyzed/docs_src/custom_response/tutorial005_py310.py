"""教程 005：PlainTextResponse 返回纯文本而非 JSON。"""

from fastapi import FastAPI
from fastapi.responses import PlainTextResponse

app = FastAPI()


@app.get("/", response_class=PlainTextResponse)  # Content-Type: text/plain
async def main():
    """返回字符串；客户端收到 text/plain 正文。"""
    return "Hello World"
