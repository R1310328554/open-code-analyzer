"""教程 009c：继承 Response 实现 CustomORJSONResponse 自定义 JSON 序列化。"""

from typing import Any

import orjson
from fastapi import FastAPI, Response

app = FastAPI()


class CustomORJSONResponse(Response):
    """自定义 JSON 响应：用 orjson 序列化，带缩进选项。"""

    media_type = "application/json"

    def render(self, content: Any) -> bytes:
        assert orjson is not None, "orjson must be installed"
        return orjson.dumps(content, option=orjson.OPT_INDENT_2)


@app.get("/", response_class=CustomORJSONResponse)
async def main():
    """仍返回 dict；FastAPI 调用 render() 生成响应体字节。"""
    return {"message": "Hello World"}
