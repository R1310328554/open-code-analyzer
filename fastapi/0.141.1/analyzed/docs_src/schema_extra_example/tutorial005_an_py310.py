"""教程 005（Annotated）：Body(openapi_examples={...})——带 summary/description 的命名示例。"""

from typing import Annotated

from fastapi import Body, FastAPI
from pydantic import BaseModel

app = FastAPI()  # 创建 FastAPI 应用实例


class Item(BaseModel):
    """商品模型；openapi_examples 为每个示例提供标题与说明。"""

    name: str
    description: str | None = None
    price: float
    tax: float | None = None


@app.put("/items/{item_id}")
async def update_item(
    *,
    item_id: int,
    item: Annotated[
        Item,
        Body(
            openapi_examples={
                "normal": {
                    "summary": "正常示例",
                    "description": "字段齐全且类型正确的 **正常** 商品。",
                    "value": {
                        "name": "Foo",
                        "description": "A very nice Item",
                        "price": 35.4,
                        "tax": 3.2,
                    },
                },
                "converted": {
                    "summary": "自动类型转换示例",
                    "description": "FastAPI 可将 price 的 `字符串` 自动转为 `数字`。",
                    "value": {
                        "name": "Bar",
                        "price": "35.4",
                    },
                },
                "invalid": {
                    "summary": "非法数据将被拒绝",
                    "value": {
                        "name": "Baz",
                        "price": "thirty five point four",
                    },
                },
            },
        ),
    ],
):
    """openapi_examples 比 examples 更富语义；/docs 下拉可切换并展示说明。"""
    results = {"item_id": item_id, "item": item}
    return results
