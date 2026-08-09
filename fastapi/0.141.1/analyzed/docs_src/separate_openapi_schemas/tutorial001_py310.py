"""教程 001：默认分离输入/输出 schema——POST 与 GET 对同一模型生成不同 OpenAPI 定义。"""

from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI()  # 默认 separate_input_output_schemas=True


class Item(BaseModel):
    """商品模型；description 可选，POST 请求体与 GET 响应共用此类型。"""

    name: str
    description: str | None = None


@app.post("/items/")
def create_item(item: Item):
    """POST 端点：OpenAPI 为输入生成 Item-Input schema（必填字段更严格）。"""
    return item


@app.get("/items/")
def read_items() -> list[Item]:
    """GET 端点：OpenAPI 为输出生成 Item-Output schema；响应可含未在请求中出现的字段。"""
    return [
        Item(
            name="Portal Gun",
            description="Device to travel through the multi-rick-verse",
        ),
        Item(name="Plumbus"),
    ]
