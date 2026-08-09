"""教程 003-05：response_model=None——禁用响应模型校验，允许 Response | dict 联合返回。"""

from fastapi import FastAPI, Response
from fastapi.responses import RedirectResponse

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/portal", response_model=None)
async def get_portal(teleport: bool = False) -> Response | dict:
    """response_model=None 告知 FastAPI 勿对返回值做 Pydantic 过滤/校验。"""
    if teleport:
        return RedirectResponse(url="https://www.youtube.com/watch?v=dQw4w9WgXcQ")
    return {"message": "Here's your interdimensional portal."}
