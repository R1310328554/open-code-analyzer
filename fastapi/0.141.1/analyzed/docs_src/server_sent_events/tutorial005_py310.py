"""教程 005：POST + SSE——接收 Prompt 请求体，逐词流式返回 token 事件。"""

from collections.abc import AsyncIterable

from fastapi import FastAPI
from fastapi.sse import EventSourceResponse, ServerSentEvent
from pydantic import BaseModel

app = FastAPI()  # 创建 FastAPI 应用实例


class Prompt(BaseModel):
    """聊天提示词；POST 请求体在流式响应开始前一次性解析。"""

    text: str


@app.post("/chat/stream", response_class=EventSourceResponse)
async def stream_chat(prompt: Prompt) -> AsyncIterable[ServerSentEvent]:
    """模拟 LLM 逐 token 输出；最后一帧 event=done 标记流结束。"""
    words = prompt.text.split()
    for word in words:
        yield ServerSentEvent(data=word, event="token")
    yield ServerSentEvent(raw_data="[DONE]", event="done")
