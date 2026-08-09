"""教程 003：pydantic.dataclasses 嵌套数据类与 response_model 组合。"""

from dataclasses import field  # (1) 可变默认值仍用 stdlib field

from fastapi import FastAPI
from pydantic.dataclasses import dataclass  # (2) Pydantic 增强 dataclass，支持校验与 OpenAPI


@dataclass
class Item:
    """嵌套子项数据类。"""
    name: str
    description: str | None = None


@dataclass
class Author:
    """作者模型，含 Item 列表。"""
    name: str
    items: list[Item] = field(default_factory=list)  # (3) 嵌套 dataclass 列表


app = FastAPI()


@app.post("/authors/{author_id}/items/", response_model=Author)  # (4) 声明响应模型为 Author
async def create_author_items(author_id: str, items: list[Item]):  # (5) 路径参数 + 请求体 Item 列表
    return {"name": author_id, "items": items}  # (6) dict 输出按 Author 校验


@app.get("/authors/", response_model=list[Author])  # (7) 响应为 Author 数组
def get_authors():  # (8) 同步路径操作函数同样可用
    return [  # (9) 嵌套 dict 自动映射为 dataclass
        {
            "name": "Breaters",
            "items": [
                {
                    "name": "Island In The Moon",
                    "description": "A place to be playin' and havin' fun",
                },
                {"name": "Holy Buddies"},
            ],
        },
        {
            "name": "System of an Up",
            "items": [
                {
                    "name": "Salt",
                    "description": "The kombucha mushroom people's favorite",
                },
                {"name": "Pad Thai"},
                {
                    "name": "Lonely Night",
                    "description": "The mostests lonliest nightiest of allest",
                },
            ],
        },
    ]
