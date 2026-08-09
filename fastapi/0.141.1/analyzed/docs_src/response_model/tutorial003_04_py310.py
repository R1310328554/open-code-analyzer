"""教程 003-04：返回类型 Response | dict——混用 Starlette Response 与普通 dict。"""

from fastapi import FastAPI, Response
from fastapi.responses import RedirectResponse

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/portal")
async def get_portal(teleport: bool = False) -> Response | dict:
    """dict 分支由 FastAPI 自动 JSON 化；RedirectResponse 分支直接作为 HTTP 响应。"""
    if teleport:
        return RedirectResponse(url="https://www.youtube.com/watch?v=dQw4w9WgXcQ")
    return {"message": "Here's your interdimensional portal."}
