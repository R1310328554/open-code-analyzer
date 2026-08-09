"""教程 003（Annotated）：list[str] Header 接收同名头的多个值（如 X-Token）。"""

from typing import Annotated

from fastapi import FastAPI, Header

app = FastAPI()


@app.get("/items/")
async def read_items(x_token: Annotated[list[str] | None, Header()] = None):
    """重复 X-Token 头会聚合为字符串列表；无该头时返回 None。"""
    return {"X-Token values": x_token}
