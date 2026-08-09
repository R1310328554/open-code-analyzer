"""教程 012（Annotated）：Annotated[list[str], Query()] 声明多值查询参数与默认列表。"""

from typing import Annotated

from fastapi import FastAPI, Query

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/")
async def read_items(q: Annotated[list[str], Query()] = ["foo", "bar"]):
    """默认值写在参数侧；Query() 承载列表型查询参数元数据。"""
    query_items = {"q": q}
    return query_items
