"""教程 006：Path 与 float 查询参数 size 的 gt/lt 数值校验（非 Annotated 写法）。"""

from fastapi import FastAPI, Path, Query

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/{item_id}")
async def read_items(
    *,
    item_id: int = Path(title="The ID of the item to get", ge=0, le=1000),  # ge/le：0 ≤ item_id ≤ 1000
    q: str,
    size: float = Query(gt=0, lt=10.5),  # 浮点 gt/lt：须大于 0 且小于 10.5
):
    """组合 path、query 与 float 数值校验示例。"""
    results = {"item_id": item_id}
    if q:
        results.update({"q": q})
    if size:
        results.update({"size": size})
    return results
