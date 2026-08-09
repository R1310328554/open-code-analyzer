"""教程 003（Annotated）：用 Annotated[Any, Depends(...)] 声明类依赖（类型信息较弱）。"""

from typing import Annotated, Any

from fastapi import Depends, FastAPI

app = FastAPI()


fake_items_db = [{"item_name": "Foo"}, {"item_name": "Bar"}, {"item_name": "Baz"}]


class CommonQueryParams:
    """依赖类：__init__ 参数自动从查询字符串注入。"""

    def __init__(self, q: str | None = None, skip: int = 0, limit: int = 100):
        self.q = q
        self.skip = skip
        self.limit = limit


@app.get("/items/")
async def read_items(commons: Annotated[Any, Depends(CommonQueryParams)]):
    """Annotated 将 Depends 与参数绑定；Any 仅作占位，IDE 提示较弱。"""
    response = {}
    if commons.q:
        response.update({"q": commons.q})
    items = fake_items_db[commons.skip : commons.skip + commons.limit]  # 按 skip/limit 切片
    response.update({"items": items})
    return response
