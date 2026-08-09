"""教程：使用 Annotated 语法声明请求体，创建时返回 201。"""

from typing import Annotated

from fastapi import Body, FastAPI, status
from fastapi.responses import JSONResponse

app = FastAPI()

# 模拟内存数据库
items = {"foo": {"name": "Fighters", "size": 6}, "bar": {"name": "Tenders", "size": 3}}


@app.put("/items/{item_id}")
async def upsert_item(
    item_id: str,
    # Annotated + Body() 声明可选请求体字段
    name: Annotated[str | None, Body()] = None,
    size: Annotated[int | None, Body()] = None,
):
    if item_id in items:
        # 已存在：更新并返回 200
        item = items[item_id]
        item["name"] = name
        item["size"] = size
        return item
    else:
        # 新建：返回 201 Created
        item = {"name": name, "size": size}
        items[item_id] = item
        return JSONResponse(status_code=status.HTTP_201_CREATED, content=item)
