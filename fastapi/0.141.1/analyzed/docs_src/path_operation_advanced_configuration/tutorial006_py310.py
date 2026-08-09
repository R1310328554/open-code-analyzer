"""教程 006：openapi_extra 自定义 requestBody  schema，配合 Request 手动读取原始体。"""

from fastapi import FastAPI, Request

app = FastAPI()


def magic_data_reader(raw_body: bytes):
    """演示性解析：忽略真实 JSON，固定返回 size 与占位 content。"""
    return {
        "size": len(raw_body),
        "content": {
            "name": "Maaaagic",
            "price": 42,
            "description": "Just kiddin', no magic here. ✨",
        },
    }


@app.post(
    "/items/",
    openapi_extra={
        "requestBody": {
            "content": {
                "application/json": {
                    "schema": {
                        "required": ["name", "price"],
                        "type": "object",
                        "properties": {
                            "name": {"type": "string"},
                            "price": {"type": "number"},
                            "description": {"type": "string"},
                        },
                    }
                }
            },
            "required": True,
        },
    },
)
async def create_item(request: Request):
    """OpenAPI 展示自定义 schema；实际用 request.body() 自行处理字节流。"""
    raw_body = await request.body()
    data = magic_data_reader(raw_body)
    return data
