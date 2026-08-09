"""教程 001：声明路径参数 item_id（未标注类型时按字符串处理）。"""

from fastapi import FastAPI

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/{item_id}")
async def read_item(item_id):
    """从 URL 路径 `/items/{item_id}` 读取 item_id 并回显。"""
    return {"item_id": item_id}
