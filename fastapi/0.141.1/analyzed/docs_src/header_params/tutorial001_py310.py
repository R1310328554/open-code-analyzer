"""教程 001：从请求 Header 读取可选 User-Agent。"""

from fastapi import FastAPI, Header

app = FastAPI()


@app.get("/items/")
async def read_items(user_agent: str | None = Header(default=None)):
    """Header() 将 User-Agent 头解析为 user_agent；未携带时返回 None。"""
    return {"User-Agent": user_agent}  # 以 JSON 回显解析到的 UA
