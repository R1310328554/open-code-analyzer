"""教程 014：Query(include_in_schema=False) 将参数从 OpenAPI/Swagger 文档中隐藏。"""

from fastapi import FastAPI, Query

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/")
async def read_items(
    hidden_query: str | None = Query(default=None, include_in_schema=False),
):
    """hidden_query 仍可正常接收 ?hidden_query=；仅不出现在自动生成的 API 文档里。"""
    if hidden_query:
        return {"hidden_query": hidden_query}
    else:
        return {"hidden_query": "Not found"}
