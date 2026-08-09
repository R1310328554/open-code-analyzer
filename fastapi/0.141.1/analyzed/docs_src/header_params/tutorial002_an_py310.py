"""教程 002（Annotated）：Header(convert_underscores=False) 保留参数名中的下划线。"""

from typing import Annotated

from fastapi import FastAPI, Header

app = FastAPI()


@app.get("/items/")
async def read_items(
    strange_header: Annotated[str | None, Header(convert_underscores=False)] = None,
):
    """默认会把 strange_header 映射为 Strange-Header；False 则按原名匹配请求头。"""
    return {"strange_header": strange_header}
