"""教程 003：list[str] Header 接收重复头名的全部值。"""

from fastapi import FastAPI, Header

app = FastAPI()


@app.get("/items/")
async def read_items(x_token: list[str] | None = Header(default=None)):
    """x_token 映射 X-Token；多个同名头合并为 list[str]。"""
    return {"X-Token values": x_token}
