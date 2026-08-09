"""教程 005：Path 声明 gt 与 le 数值约束（非 Annotated 写法）。"""

from fastapi import FastAPI, Path

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/{item_id}")
async def read_items(
    *,
    item_id: int = Path(title="The ID of the item to get", gt=0, le=1000),  # gt/le：须满足 0 < item_id ≤ 1000
    q: str,
):
    """返回 item_id 与查询 q；超出 gt/le 范围时返回 422。"""
    results = {"item_id": item_id}
    if q:
        results.update({"q": q})
    return results
