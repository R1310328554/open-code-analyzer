"""教程 002：为路径参数标注 int 类型，FastAPI 自动校验与转换。"""

from fastapi import FastAPI

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/{item_id}")
async def read_item(item_id: int):
    """item_id 必须为整数；非数字路径会返回 422 校验错误。"""
    return {"item_id": item_id}
