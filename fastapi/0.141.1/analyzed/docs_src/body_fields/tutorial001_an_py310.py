"""教程 001（Annotated）：Body(embed=True) 嵌套 body，Field 声明校验与 OpenAPI 元数据。"""

from typing import Annotated

from fastapi import Body, FastAPI
from pydantic import BaseModel, Field

app = FastAPI()


class Item(BaseModel):
    """Item 模型；部分字段通过 Field 附加 title、长度与数值约束。"""
    name: str
    description: str | None = Field(
        default=None, title="The description of the item", max_length=300
    )
    price: float = Field(gt=0, description="The price must be greater than zero")
    tax: float | None = None


@app.put("/items/{item_id}")
async def update_item(item_id: int, item: Annotated[Item, Body(embed=True)]):
    """更新物品；embed=True 使 JSON body 形如 {"item": {...}} 而非顶层字段。"""
    results = {"item_id": item_id, "item": item}
    return results
