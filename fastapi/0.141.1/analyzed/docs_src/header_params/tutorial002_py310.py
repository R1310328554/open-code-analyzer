"""教程 002：非 Annotated 写法——Header(convert_underscores=False) 禁用下划线转连字符。"""

from fastapi import FastAPI, Header

app = FastAPI()


@app.get("/items/")
async def read_items(
    strange_header: str | None = Header(default=None, convert_underscores=False),
):
    """与 Annotated 版等价：请求头名需与参数名一致（含下划线）。"""
    return {"strange_header": strange_header}
