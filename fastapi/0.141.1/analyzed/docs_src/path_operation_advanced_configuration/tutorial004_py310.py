"""教程 004：summary 与函数 docstring 丰富 OpenAPI 操作描述与字段说明。"""

from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI()


class Item(BaseModel):
    """创建 item 的请求/响应模型。"""
    name: str
    description: str | None = None
    price: float
    tax: float | None = None
    tags: set[str] = set()


@app.post("/items/", summary="Create an item")
async def create_item(item: Item) -> Item:
    """
    创建 item，字段说明如下：

    - **name**：每个 item 必须有名称
    - **description**：较长描述
    - **price**：必填
    - **tax**：无税时可省略
    - **tags**：该 item 的唯一标签字符串集合
    
    :param item: 用户提交的请求体。
    """
    return item
