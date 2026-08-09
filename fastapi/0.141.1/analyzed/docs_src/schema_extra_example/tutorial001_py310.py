"""教程 001：model_config json_schema_extra——在模型级 OpenAPI schema 嵌入 examples。"""

from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI()  # 创建 FastAPI 应用实例


class Item(BaseModel):
    """model_config["json_schema_extra"]["examples"] 写入 Swagger UI 示例请求体。"""

    name: str
    description: str | None = None
    price: float
    tax: float | None = None

    model_config = {
        "json_schema_extra": {
            "examples": [
                {
                    "name": "Foo",
                    "description": "A very nice Item",
                    "price": 35.4,
                    "tax": 3.2,
                }
            ]
        }
    }


@app.put("/items/{item_id}")
async def update_item(item_id: int, item: Item):
    """Item 的 schema 示例会出现在 /docs 交互界面，便于客户端理解字段格式。"""
    results = {"item_id": item_id, "item": item}
    return results
