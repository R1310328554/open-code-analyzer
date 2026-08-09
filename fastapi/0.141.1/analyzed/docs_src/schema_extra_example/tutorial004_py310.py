"""教程 004：Body(examples=[...]) 多示例——非 Annotated 写法，效果与 tutorial004_an 相同。"""

from fastapi import Body, FastAPI
from pydantic import BaseModel

app = FastAPI()  # 创建 FastAPI 应用实例


class Item(BaseModel):
    """商品字段；多个 examples 供 /docs 切换预览不同请求体。"""

    name: str
    description: str | None = None
    price: float
    tax: float | None = None


@app.put("/items/{item_id}")
async def update_item(
    *,
    item_id: int,
    item: Item = Body(
        examples=[
            {
                "name": "Foo",
                "description": "A very nice Item",
                "price": 35.4,
                "tax": 3.2,
            },
            {
                "name": "Bar",
                "price": "35.4",
            },
            {
                "name": "Baz",
                "price": "thirty five point four",
            },
        ],
    ),
):
    """关键字-only 参数 + Body 多示例；Pydantic 会尝试将 price 字符串转为 float。"""
    results = {"item_id": item_id, "item": item}
    return results
