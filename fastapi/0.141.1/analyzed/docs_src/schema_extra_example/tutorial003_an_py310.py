"""教程 003（Annotated）：Body(examples=[...])——为单个请求体参数附加 OpenAPI 示例。"""

from typing import Annotated

from fastapi import Body, FastAPI
from pydantic import BaseModel

app = FastAPI()  # 创建 FastAPI 应用实例


class Item(BaseModel):
    """请求体模型；示例通过 Annotated + Body 绑定到 item 参数。"""

    name: str
    description: str | None = None
    price: float
    tax: float | None = None


@app.put("/items/{item_id}")
async def update_item(
    item_id: int,
    item: Annotated[
        Item,
        Body(
            examples=[
                {
                    "name": "Foo",
                    "description": "A very nice Item",
                    "price": 35.4,
                    "tax": 3.2,
                }
            ],
        ),
    ],
):
    """Body(examples=...) 覆盖该 endpoint 请求体示例；适合多参数时单独标注。"""
    results = {"item_id": item_id, "item": item}
    return results
