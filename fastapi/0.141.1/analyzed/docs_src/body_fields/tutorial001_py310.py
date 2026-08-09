"""教程 001：Body(embed=True) 嵌套 body 与 Field 校验（传统 Body 默认参数语法）。"""

from fastapi import Body, FastAPI
from pydantic import BaseModel, Field

app = FastAPI()


class Item(BaseModel):
    """Item 模型；Field 为 description、price 等字段提供 OpenAPI 与校验规则。"""
    name: str
    description: str | None = Field(
        default=None, title="The description of the item", max_length=300
    )
    price: float = Field(gt=0, description="The price must be greater than zero")
    tax: float | None = None


@app.put("/items/{item_id}")
async def update_item(item_id: int, item: Item = Body(embed=True)):
    """更新物品；body 须嵌套在 item 键下，便于与路径参数区分。"""
    results = {"item_id": item_id, "item": item}
    return results
