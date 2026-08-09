"""教程：在 responses 中声明多种状态码及 image/png 等非 JSON 内容类型。"""

from fastapi import FastAPI
from fastapi.responses import FileResponse
from pydantic import BaseModel


class Item(BaseModel):
    """Item 响应模型。"""
    id: str
    value: str


# 共享的附加响应描述，供 OpenAPI 文档展示
responses = {
    404: {"description": "Item not found"},
    302: {"description": "The item was moved"},
    403: {"description": "Not enough privileges"},
}


app = FastAPI()


@app.get(
    "/items/{item_id}",
    response_model=Item,
    # 合并共享 responses，并为 200 声明 image/png 媒体类型
    responses={**responses, 200: {"content": {"image/png": {}}}},
)
async def read_item(item_id: str, img: bool | None = None):
    if img:
        # img=true 时返回 PNG 文件而非 JSON
        return FileResponse("image.png", media_type="image/png")
    else:
        return {"id": "foo", "value": "there goes my hero"}
