"""教程 001：EventSourceResponse 基础——yield Pydantic 模型自动编码为 SSE data 字段。"""

from collections.abc import AsyncIterable, Iterable

from fastapi import FastAPI
from fastapi.sse import EventSourceResponse
from pydantic import BaseModel

app = FastAPI()  # 创建 FastAPI 应用实例


class Item(BaseModel):
    """流式推送的商品条目；yield 时自动 JSON 序列化为 SSE 的 data:"""

    name: str
    description: str | None


items = [
    Item(name="Plumbus", description="A multi-purpose household device."),
    Item(name="Portal Gun", description="A portal opening device."),
    Item(name="Meeseeks Box", description="A box that summons a Meeseeks."),
]


@app.get("/items/stream", response_class=EventSourceResponse)
async def sse_items() -> AsyncIterable[Item]:
    """异步生成器 + 返回类型注解；每个 Item 编码为一帧 text/event-stream。"""
    for item in items:
        yield item


@app.get("/items/stream-no-async", response_class=EventSourceResponse)
def sse_items_no_async() -> Iterable[Item]:
    """同步生成器同样支持；FastAPI 在后台线程中迭代 yield。"""
    for item in items:
        yield item


@app.get("/items/stream-no-annotation", response_class=EventSourceResponse)
async def sse_items_no_annotation():
    """无返回类型注解时仍可工作；OpenAPI 无法推断流式 payload 结构。"""
    for item in items:
        yield item


@app.get("/items/stream-no-async-no-annotation", response_class=EventSourceResponse)
def sse_items_no_async_no_annotation():
    """同步 + 无注解的最简写法；生产环境建议保留 AsyncIterable[Item] 注解。"""
    for item in items:
        yield item
