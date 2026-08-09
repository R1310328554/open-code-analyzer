"""教程 003：ServerSentEvent(raw_data=...)——发送原始文本行，跳过 JSON 序列化。"""

from collections.abc import AsyncIterable

from fastapi import FastAPI
from fastapi.sse import EventSourceResponse, ServerSentEvent

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/logs/stream", response_class=EventSourceResponse)
async def stream_logs() -> AsyncIterable[ServerSentEvent]:
    """raw_data 直接写入 data: 行，适合日志、纯文本等非 JSON 内容。"""
    logs = [
        "2025-01-01 INFO  Application started",
        "2025-01-01 DEBUG Connected to database",
        "2025-01-01 WARN  High memory usage detected",
    ]
    for log_line in logs:
        yield ServerSentEvent(raw_data=log_line)
