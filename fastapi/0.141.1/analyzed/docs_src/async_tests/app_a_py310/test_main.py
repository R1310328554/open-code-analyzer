"""使用 httpx AsyncClient 对 app_a 进行异步 HTTP 测试。"""

import pytest
from httpx import ASGITransport, AsyncClient

from .main import app


@pytest.mark.anyio
async def test_root():
    """异步 GET 根路径，断言 200 与 JSON 内容。"""
    # ASGITransport 将请求直接转发到 FastAPI app，无需真实网络
    async with AsyncClient(
        transport=ASGITransport(app=app), base_url="http://test"
    ) as ac:
        response = await ac.get("/")
    assert response.status_code == 200
    assert response.json() == {"message": "Tomato"}
