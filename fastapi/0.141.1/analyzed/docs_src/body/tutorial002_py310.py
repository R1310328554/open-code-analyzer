"""教程 002：读取 body 后计算含税价并返回扩展字段。"""

from fastapi import FastAPI
from pydantic import BaseModel


class Item(BaseModel):
    """Item 请求体模型（含可选 tax）。"""
    name: str
    description: str | None = None
    price: float
    tax: float | None = None


app = FastAPI()


@app.post("/items/")
async def create_item(item: Item):
    """解析 body，若有 tax 则附加 price_with_tax 后返回 dict。"""
    # 转为普通 dict 以便添加计算字段
    item_dict = item.model_dump()
    # 仅在提供 tax 时计算含税价
    if item.tax is not None:
        price_with_tax = item.price + item.tax
        item_dict.update({"price_with_tax": price_with_tax})
    return item_dict
