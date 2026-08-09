"""教程 004：用 `*` 强制后续参数以关键字传入；Path 的 ge=1 要求 item_id ≥ 1。"""

"""教程 004：用 `*` 强制后续参数以关键字传入；Path 的 ge=1 要求 item_id ≥ 1。"""

"""教程 004：用 `*` 强制后续参数以关键字传入；Path 的 ge=1 要求 item_id ≥ 1。"""

from fastapi import FastAPI, Path

app = FastAPI()  # 创建 FastAPI 应用实例  # 创建 FastAPI 应用实例  # 创建 FastAPI 应用实例


@app.get("/items/{item_id}")
async def read_items(
    *,
    item_id: int = Path(title="The ID of the item to get", ge=1),  # ge：greater than or equal，≥1
    q: str,  # 必填查询参数；* 使其必须以关键字形式传入
):
    """读取 item_id 与查询 q；item_id 不满足 ge 时返回 422。"""
    results = {"item_id": item_id}
    if q:
        results.update({"q": q})
    return results
