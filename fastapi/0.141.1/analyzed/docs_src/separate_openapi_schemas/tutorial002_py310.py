"""教程 002：separate_input_output_schemas=False——输入与输出共用同一 OpenAPI schema。"""

from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI(separate_input_output_schemas=False)  # 关闭输入/输出 schema 分离


class Item(BaseModel):
    """商品字段；关闭分离后 POST 与 GET 在 /docs 中显示相同的 Item schema。"""

    name: str
    description: str | None = None


@app.post("/items/")
def create_item(item: Item):
    """与 tutorial001 逻辑相同；OpenAPI 不再区分 Item-Input / Item-Output。"""
    return item


@app.get("/items/")
def read_items() -> list[Item]:
    """适合输入输出结构完全一致的场景；简化文档但失去细粒度 schema 差异。"""
    return [
        Item(
            name="Portal Gun",
            description="Device to travel through the multi-rick-verse",
        ),
        Item(name="Plumbus"),
    ]
