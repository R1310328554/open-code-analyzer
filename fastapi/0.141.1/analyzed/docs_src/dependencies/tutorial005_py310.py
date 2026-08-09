"""教程 005：子依赖链的非 Annotated 写法，q 依赖 query_extractor 并配合 Cookie。"""

from fastapi import Cookie, Depends, FastAPI

app = FastAPI()


def query_extractor(q: str | None = None):
    """一级依赖：从查询参数 q 取值。"""
    return q


def query_or_cookie_extractor(
    q: str = Depends(query_extractor), last_query: str | None = Cookie(default=None)
):
    """二级依赖：q 为空时回退到 Cookie last_query。"""
    if not q:
        return last_query
    return q


@app.get("/items/")
async def read_query(query_or_default: str = Depends(query_or_cookie_extractor)):
    """Depends 参数可声明对其他依赖函数的引用，形成依赖树。"""
    return {"q_or_cookie": query_or_default}
