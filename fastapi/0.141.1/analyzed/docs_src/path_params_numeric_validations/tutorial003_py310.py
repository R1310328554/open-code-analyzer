"""教程 003：`*` 强制关键字参数，消除顺序歧义——item_id 为路径参数，q 为查询参数。"""

from fastapi import FastAPI, Path

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/{item_id}")
async def read_items(*, item_id: int = Path(title="The ID of the item to get"), q: str):
    """`*` 之后参数必须按名传递，避免与 tutorial002 的顺序陷阱混淆。"""
    results = {"item_id": item_id}
    if q:
        results.update({"q": q})
    return results
