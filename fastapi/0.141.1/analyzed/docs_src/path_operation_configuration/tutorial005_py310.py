"""教程 005：用 response_description 描述成功响应的含义（OpenAPI 响应说明）。"""

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


@app.post(
    "/items/",
    summary="Create an item",
    response_description="The created item",  # 200 响应在文档中的说明文字
)
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
