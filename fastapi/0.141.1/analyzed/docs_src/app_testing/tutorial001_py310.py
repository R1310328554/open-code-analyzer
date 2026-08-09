"""教程 001：在同一文件中定义 FastAPI 应用并用 TestClient 测试 HTTP GET。"""

from fastapi import FastAPI
from fastapi.testclient import TestClient

app = FastAPI()


@app.get("/")
async def read_main():
    return {"msg": "Hello World"}


# 模块级 TestClient，测试函数共享同一 app 实例
client = TestClient(app)


def test_read_main():
    """验证根路径返回 200 与预期 JSON。"""
    response = client.get("/")
    assert response.status_code == 200
    assert response.json() == {"msg": "Hello World"}
