"""教程：测试 lifespan 上下文管理器——启动填充、退出清理。"""

from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.testclient import TestClient

# 模块级共享状态，由 lifespan 管理生命周期
items = {}


@asynccontextmanager
async def lifespan(app: FastAPI):
    """应用 lifespan：启动时填充 items，关闭时清空。"""
    items["foo"] = {"name": "Fighters"}
    items["bar"] = {"name": "Tenders"}
    yield
    # lifespan 结束时清理 items
    items.clear()


# 将 lifespan 注册到 FastAPI 应用
app = FastAPI(lifespan=lifespan)


@app.get("/items/{item_id}")
async def read_items(item_id: str):
    """按 ID 读取 lifespan 阶段填充的 item。"""
    return items[item_id]


def test_read_items():
    """验证 lifespan 在 TestClient 生命周期内的启动与清理行为。"""
    # lifespan 启动前 items 仍为空
    assert items == {}

    with TestClient(app) as client:
        # with 块内 lifespan 已启动，items 已填充
        assert items == {"foo": {"name": "Fighters"}, "bar": {"name": "Tenders"}}

        response = client.get("/items/foo")
        assert response.status_code == 200
        assert response.json() == {"name": "Fighters"}

        # 请求完成后 items 仍保留
        assert items == {"foo": {"name": "Fighters"}, "bar": {"name": "Tenders"}}

    # 退出 with 块模拟应用终止，lifespan 结束并清理 items
    assert items == {}
