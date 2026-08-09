"""教程 002：Field(examples=[...])——逐字段在 OpenAPI schema 中声明示例值。"""

from fastapi import FastAPI
from pydantic import BaseModel, Field

app = FastAPI()  # 创建 FastAPI 应用实例


class Item(BaseModel):
    """每个 Field 的 examples 会独立写入对应属性的 schema 元数据。"""

    name: str = Field(examples=["Foo"])
    description: str | None = Field(default=None, examples=["A very nice Item"])
    price: float = Field(examples=[35.4])
    tax: float | None = Field(default=None, examples=[3.2])


@app.put("/items/{item_id}")
async def update_item(item_id: int, item: Item):
    """字段级 examples 比 model_config 更细粒度；Swagger 按属性展示示例。"""
    results = {"item_id": item_id, "item": item}
    return results
