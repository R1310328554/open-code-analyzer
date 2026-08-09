"""教程 002：可选查询参数 q——`str | None = None` 表示可省略。"""

from fastapi import FastAPI

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/{item_id}")
async def read_item(item_id: str, q: str | None = None):
    """item_id 来自路径；q 来自 ?q= 查询串，省略时 q 为 None。"""
    if q:
        return {"item_id": item_id, "q": q}
    return {"item_id": item_id}
