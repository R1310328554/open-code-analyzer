"""教程 001：operation_id 自定义 OpenAPI 中该操作的唯一标识符。"""

from fastapi import FastAPI

app = FastAPI()


@app.get("/items/", operation_id="some_specific_id_you_define")
async def read_items():
    """默认 operation_id 由函数名推导；显式指定便于客户端代码生成与文档引用。"""
    return [{"item_id": "Foo"}]
