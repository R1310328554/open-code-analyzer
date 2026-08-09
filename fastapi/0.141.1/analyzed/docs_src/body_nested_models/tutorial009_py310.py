"""教程 009：请求体为 dict[int, float]（索引 → 权重映射）。"""

from fastapi import FastAPI

app = FastAPI()


@app.post("/index-weights/")
async def create_index_weights(weights: dict[int, float]):
    """接收键为 int、值为 float 的字典 body；JSON 键会转为整数。"""
    return weights
