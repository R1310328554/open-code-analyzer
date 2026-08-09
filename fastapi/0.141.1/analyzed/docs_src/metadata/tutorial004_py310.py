"""教程 004：用 openapi_tags 为 OpenAPI 分组（tag）提供名称、描述与 externalDocs。"""

from fastapi import FastAPI

tags_metadata = [
    {
        "name": "users",
        "description": "与用户相关的操作；**登录**逻辑也包含在此分组。",
    },
    {
        "name": "items",
        "description": "管理物品。它们非常 _fancy_，因此有独立的外部文档。",
        "externalDocs": {
            "description": "Items 外部文档",
            "url": "https://fastapi.tiangolo.com/",
        },
    },
]

app = FastAPI(openapi_tags=tags_metadata)  # 将 tags_metadata 注入 OpenAPI schema


@app.get("/users/", tags=["users"])
async def get_users():
    """列出用户；在文档中归入 users 分组。"""
    return [{"name": "Harry"}, {"name": "Ron"}]


@app.get("/items/", tags=["items"])
async def get_items():
    """列出物品；在文档中归入 items 分组。"""
    return [{"name": "wand"}, {"name": "flying broom"}]
