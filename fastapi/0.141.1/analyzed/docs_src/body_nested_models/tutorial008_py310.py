"""教程 008：请求体直接为 list[Image]，非包裹在 Pydantic 模型内。"""

from fastapi import FastAPI
from pydantic import BaseModel, HttpUrl

app = FastAPI()


class Image(BaseModel):
    """图片子模型：URL 与名称。"""
    url: HttpUrl
    name: str


@app.post("/images/multiple/")
async def create_multiple_images(images: list[Image]):
    """body 顶层为 JSON 数组，每项解析为 Image。"""
    return images
