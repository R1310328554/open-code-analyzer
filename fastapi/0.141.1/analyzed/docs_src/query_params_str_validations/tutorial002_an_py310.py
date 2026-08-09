"""教程 002（Annotated）：Query(max_length=50) 限制 q 最长 50 字符，超长返回 422。"""

from typing import Annotated

from fastapi import FastAPI, Query

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/")
async def read_items(q: Annotated[str | None, Query(max_length=50)] = None):
    """Annotated 将校验元数据与类型绑定；与 tutorial002 传统写法等价。"""
    results = {"items": [{"item_id": "Foo"}, {"item_id": "Bar"}]}
    if q:
        results.update({"q": q})
    return results
