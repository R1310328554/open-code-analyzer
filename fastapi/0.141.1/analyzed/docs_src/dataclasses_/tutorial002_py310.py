"""教程 002：dataclass 含可变默认值（field default_factory）与 response_model。"""

from dataclasses import dataclass, field

from fastapi import FastAPI


@dataclass
class Item:
    """Item 含 tags 列表；default_factory 避免可变默认值陷阱。"""
    name: str
    price: float
    tags: list[str] = field(default_factory=list)
    description: str | None = None
    tax: float | None = None


app = FastAPI()


@app.get("/items/next", response_model=Item)  # 声明响应按 Item 过滤/校验
async def read_next_item():
    """返回 dict；FastAPI 按 response_model=Item 校验输出字段。"""
    return {
        "name": "Island In The Moon",
        "price": 12.99,
        "description": "A place to be playin' and havin' fun",
        "tags": ["breater"],
    }
