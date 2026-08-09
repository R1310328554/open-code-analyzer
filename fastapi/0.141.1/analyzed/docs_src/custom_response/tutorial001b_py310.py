"""教程 001b：直接返回 ORJSONResponse 实例（绕过默认 JSONResponse）。"""

from fastapi import FastAPI
from fastapi.responses import ORJSONResponse

app = FastAPI()


@app.get("/items/", response_class=ORJSONResponse)  # OpenAPI 仍标注 ORJSON 类型
async def read_items():
    """显式构造 ORJSONResponse；适合需精细控制响应头的场景。"""
    return ORJSONResponse([{"item_id": "Foo"}])
