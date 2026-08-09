"""教程：更新返回 200，新建时通过 JSONResponse 返回 201 Created。"""

from fastapi import Body, FastAPI, status
from fastapi.responses import JSONResponse

app = FastAPI()

# 模拟内存数据库
items = {"foo": {"name": "Fighters", "size": 6}, "bar": {"name": "Tenders", "size": 3}}


@app.put("/items/{item_id}")
async def upsert_item(
    item_id: str,
    name: str | None = Body(default=None),
    size: int | None = Body(default=None),
):
    if item_id in items:
        # 已存在：更新字段，默认返回 200 OK
        item = items[item_id]
        item["name"] = name
        item["size"] = size
        return item
    else:
        # 新建条目：显式设置 201 Created 状态码
        item = {"name": name, "size": size}
        items[item_id] = item
        return JSONResponse(status_code=status.HTTP_201_CREATED, content=item)
