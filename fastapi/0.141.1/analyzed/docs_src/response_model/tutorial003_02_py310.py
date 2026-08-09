"""教程 003-02：返回类型 Response——同一 endpoint 可返回 JSONResponse 或 RedirectResponse。"""

from fastapi import FastAPI, Response
from fastapi.responses import JSONResponse, RedirectResponse

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/portal")
async def get_portal(teleport: bool = False) -> Response:
    """teleport=True 时 307 重定向；否则返回 JSONResponse 正文。"""
    if teleport:
        return RedirectResponse(url="https://www.youtube.com/watch?v=dQw4w9WgXcQ")
    return JSONResponse(content={"message": "Here's your interdimensional portal."})
