"""app02 测试：用 dependency_overrides 替换 get_settings，隔离真实环境变量。"""

from fastapi.testclient import TestClient

from .config import Settings
from .main import app, get_settings

client = TestClient(app)  # 基于 app02 main 模块创建测试客户端


def get_settings_override():
    """测试替身：固定 admin_email，无需设置真实 ADMIN_EMAIL 环境变量。"""
    return Settings(admin_email="testing_admin@example.com")


app.dependency_overrides[get_settings] = get_settings_override  # 键为原依赖函数


def test_app():
    """GET /info 应返回 override 后的配置，而非 .env 或系统环境中的值。"""
    response = client.get("/info")
    data = response.json()
    assert data == {
        "app_name": "Awesome API",
        "admin_email": "testing_admin@example.com",
        "items_per_user": 50,
    }
