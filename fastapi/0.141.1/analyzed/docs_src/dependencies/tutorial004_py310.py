"""教程 004：CommonQueryParams = Depends() 简写，类型注解与 Depends() 配合。"""

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
async def read_items(commons: CommonQueryParams = Depends()):
    """Depends() 从左侧类型注解推断 CommonQueryParams，等价于 Depends(CommonQueryParams)。"""
    response = {}
    if commons.q:
        response.update({"q": commons.q})
    items = fake_items_db[commons.skip : commons.skip + commons.limit]  # 按 skip/limit 切片
    response.update({"items": items})
    return response
