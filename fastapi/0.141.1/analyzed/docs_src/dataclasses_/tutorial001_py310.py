"""教程 001：标准库 @dataclass 作为请求体，FastAPI 自动校验与序列化。"""

from dataclasses import dataclass

from fastapi import FastAPI


@dataclass
class Item:
    """商品数据类；可选字段 description、tax 默认为 None。"""
    name: str
    price: float
    description: str | None = None
    tax: float | None = None


app = FastAPI()


@app.post("/items/")
async def create_item(item: Item):
    """接收 JSON 请求体并映射为 Item；原样返回以演示响应序列化。"""
    return item
