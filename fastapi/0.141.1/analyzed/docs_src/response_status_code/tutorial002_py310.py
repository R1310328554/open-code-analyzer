"""教程 002：使用 status.HTTP_201_CREATED 常量代替裸数字，可读性与 IDE 补全更好。"""

from fastapi import FastAPI, status

app = FastAPI()  # 创建 FastAPI 应用实例


@app.post("/items/", status_code=status.HTTP_201_CREATED)
async def create_item(name: str):
    """status 模块提供命名常量；与 tutorial001 行为一致。"""
    return {"name": name}
