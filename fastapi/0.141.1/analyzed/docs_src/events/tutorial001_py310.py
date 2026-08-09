"""教程 001：@app.on_event("startup")  # 首个请求到达前执行 在应用启动时预加载数据。"""

from fastapi import FastAPI

app = FastAPI()

# 应用级共享状态，startup 时填充
items = {}


@app.on_event("startup")
async def startup_event():
    """启动钩子：初始化 items 字典，供后续路由读取。"""
    items["foo"] = {"name": "Fighters"}  # 预置示例数据
    items["bar"] = {"name": "Tenders"}


@app.get("/items/{item_id}")
async def read_items(item_id: str):
    """读取 startup 阶段写入的 item。"""
    return items[item_id]
