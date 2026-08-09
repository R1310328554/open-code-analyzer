"""教程 005：response_model=dict[str, float]——声明响应为字符串键、浮点值的字典。"""

from fastapi import FastAPI

app = FastAPI()


@app.get("/keyword-weights/", response_model=dict[str, float])
async def read_keyword_weights():
    """返回关键词权重映射；FastAPI 校验键为 str、值为 float。"""
    return {"foo": 2.3, "bar": 3.4}
