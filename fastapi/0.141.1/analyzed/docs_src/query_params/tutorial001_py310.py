"""教程 001：查询参数 skip、limit 带默认值——用于分页切片 fake_items_db。"""

from fastapi import FastAPI

app = FastAPI()  # 创建 FastAPI 应用实例

fake_items_db = [{"item_name": "Foo"}, {"item_name": "Bar"}, {"item_name": "Baz"}]


@app.get("/items/")
async def read_item(skip: int = 0, limit: int = 10):
    """skip/limit 来自 ?skip=&limit= 查询串；未传时使用默认值 0 与 10。"""
    return fake_items_db[skip : skip + limit]
