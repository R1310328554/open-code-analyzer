"""教程 003-03：返回类型 RedirectResponse——FastAPI 跳过 JSON 序列化，直接发送重定向。"""

from fastapi import FastAPI
from fastapi.responses import RedirectResponse

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/teleport")
async def get_teleport() -> RedirectResponse:
    """明确返回 RedirectResponse；OpenAPI 文档会标注 307 Temporary Redirect。"""
    return RedirectResponse(url="https://www.youtube.com/watch?v=dQw4w9WgXcQ")
