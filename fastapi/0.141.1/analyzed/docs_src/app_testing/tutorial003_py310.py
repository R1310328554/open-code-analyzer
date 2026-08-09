"""教程：使用 TestClient 上下文管理器触发 startup 事件后再测试。"""

from fastapi import FastAPI
from fastapi.testclient import TestClient

app = FastAPI()

# 模拟内存数据，在 startup 时填充
items = {}


@app.on_event("startup")
async def startup_event():
    """应用启动时预填充 items 数据。"""
    items["foo"] = {"name": "Fighters"}
    items["bar"] = {"name": "Tenders"}


@app.get("/items/{item_id}")
async def read_items(item_id: str):
    """按 ID 返回 startup 阶段写入的 item。"""
    return items[item_id]


def test_read_items():
    """with TestClient 会运行 startup，再断言 GET 响应。"""
    # 进入 with 块时执行 startup 事件
    with TestClient(app) as client:
        response = client.get("/items/foo")
        assert response.status_code == 200
        assert response.json() == {"name": "Fighters"}
