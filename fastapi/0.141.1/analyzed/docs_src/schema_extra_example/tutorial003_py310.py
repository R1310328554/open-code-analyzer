"""教程 003：Body(examples=[...])——在请求体参数上声明单个 OpenAPI 示例。"""

from fastapi import Body, FastAPI
from pydantic import BaseModel

app = FastAPI()  # 创建 FastAPI 应用实例


class Item(BaseModel):
    """商品模型：name/price 必填；description 与 tax 可选。"""

    name: str
    description: str | None = None
    price: float
    tax: float | None = None


@app.put("/items/{item_id}")
async def update_item(
    item_id: int,
    item: Item = Body(
        examples=[
            {
                "name": "Foo",
                "description": "A very nice Item",
                "price": 35.4,
                "tax": 3.2,
            }
        ],
    ),
):
    """Body(examples=...) 将示例写入 OpenAPI；Swagger UI 可一键填充请求体。"""
    results = {"item_id": item_id, "item": item}
    return results
