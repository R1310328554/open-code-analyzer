"""教程 004：summary 写在装饰器上，详细说明写在函数 docstring（会出现在 OpenAPI 文档中）。"""

from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI()


class Item(BaseModel):
    """物品数据模型。"""
    name: str
    description: str | None = None
    price: float
    tax: float | None = None
    tags: set[str] = set()


@app.post("/items/", summary="Create an item")  # 简短摘要仍在装饰器参数中
async def create_item(item: Item) -> Item:
    """
    创建包含完整信息的物品：

    - **name**：每个物品必须有名称
    - **description**：较长描述
    - **price**：必填
    - **tax**：若无税可省略
    - **tags**：该物品的一组唯一标签字符串
    """
    return item
