"""教程 006：必填与可选查询参数混用——needy 必填，skip/limit 可选。"""

from fastapi import FastAPI

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/{item_id}")
async def read_user_item(
    item_id: str, needy: str, skip: int = 0, limit: int | None = None
):
    """limit 可为 None（未传）；skip 默认 0；needy 始终必填。"""
    item = {"item_id": item_id, "needy": needy, "skip": skip, "limit": limit}
    return item
