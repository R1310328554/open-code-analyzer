"""教程 002：无默认值的 q 在前会被视为查询参数；item_id 须用 Path() 显式标记。"""

from fastapi import FastAPI, Path

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/{item_id}")
async def read_items(q: str, item_id: int = Path(title="The ID of the item to get")):
    """与 Annotated 版等价：q 来自查询串，item_id 来自路径 {item_id}。"""
    results = {"item_id": item_id}
    if q:
        results.update({"q": q})
    return results
