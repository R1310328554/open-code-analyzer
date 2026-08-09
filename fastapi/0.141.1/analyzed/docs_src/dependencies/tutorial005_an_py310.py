"""教程 005（Annotated）：子依赖链——query_extractor 被 query_or_cookie_extractor 复用。"""

from typing import Annotated

from fastapi import Cookie, Depends, FastAPI

app = FastAPI()


def query_extractor(q: str | None = None):
    """一级依赖：从查询参数 q 取值。"""
    return q


def query_or_cookie_extractor(
    q: Annotated[str, Depends(query_extractor)],
    last_query: Annotated[str | None, Cookie()] = None,
):
    """二级依赖：q 为空时回退到 Cookie last_query。"""
    if not q:
        return last_query
    return q


@app.get("/items/")
async def read_query(
    query_or_default: Annotated[str, Depends(query_or_cookie_extractor)],
):
    """Depends 可嵌套；FastAPI 先解析子依赖再注入外层。"""
    return {"q_or_cookie": query_or_default}
