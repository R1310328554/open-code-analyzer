"""教程 002：可调用类作为依赖，显式类型注解配合 Depends(CommonQueryParams)。"""

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
async def read_items(commons: CommonQueryParams = Depends(CommonQueryParams)):
    """Depends(CommonQueryParams) 实例化类并注入 commons。"""
    response = {}
    if commons.q:
        response.update({"q": commons.q})
    items = fake_items_db[commons.skip : commons.skip + commons.limit]  # 按 skip/limit 切片
    response.update({"items": items})
    return response
