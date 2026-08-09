"""教程 001：Path(...) 声明路径约束，可选 q 与 item（关键字-only 参数）。"""

from fastapi import FastAPI, Path
from pydantic import BaseModel

app = FastAPI()


class Item(BaseModel):
    """Item 请求体模型（可选）。"""
    name: str
    description: str | None = None
    price: float
    tax: float | None = None


@app.put("/items/{item_id}")
async def update_item(
    *,
    item_id: int = Path(title="The ID of the item to get", ge=0, le=1000),
    q: str | None = None,
    item: Item | None = None,
):
    """* 强制 keyword-only，避免 Path 默认值与 body 参数顺序混淆。"""
    results = {"item_id": item_id}
    if q:
        results.update({"q": q})
    if item:
        results.update({"item": item})
    return results
