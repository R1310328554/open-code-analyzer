"""教程 002：HTTPException 自定义响应头——404 时在 headers 中附加 X-Error。"""

from fastapi import FastAPI, HTTPException

app = FastAPI()

items = {"foo": "The Foo Wrestlers"}


@app.get("/items-header/{item_id}")
async def read_item_header(item_id: str):
    """404 时除 detail 外还可设置自定义 HTTP 头供客户端或代理识别。"""
    if item_id not in items:
        raise HTTPException(
            status_code=404,
            detail="Item not found",
            headers={"X-Error": "There goes my error"},
        )
    return {"item": items[item_id]}
