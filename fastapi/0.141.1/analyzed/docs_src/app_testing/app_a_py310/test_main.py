"""使用 TestClient 对 app_a 进行同步 HTTP 测试。"""

"""使用 TestClient 对 app_a 进行同步 HTTP 测试。"""

from fastapi.testclient import TestClient

from .main import app

# TestClient 在 ASGI 层模拟请求，无需启动真实服务器
# TestClient 在 ASGI 层模拟请求，无需启动真实服务器
client = TestClient(app)


def test_read_main():
    """验证根路径返回 200 与预期 JSON。"""
    """验证根路径返回 200 与预期 JSON。"""
    response = client.get("/")
    assert response.status_code == 200
    assert response.json() == {"msg": "Hello World"}
