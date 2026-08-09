"""示例 02 测试：dependency_overrides 替换 get_settings，无需改生产代码或环境变量。"""

from fastapi.testclient import TestClient

from .config import Settings
from .main import app, get_settings

client = TestClient(app)


def get_settings_override():
    """测试用 Settings：固定 admin_email，绕过真实环境变量。"""
    return Settings(admin_email="testing_admin@example.com")


app.dependency_overrides[get_settings] = get_settings_override  # 覆盖依赖，仅影响本测试客户端


def test_app():
    """GET /info 应返回 override 后的 admin_email，其余字段保持默认。"""
    response = client.get("/info")
    data = response.json()
    assert data == {
        "app_name": "Awesome API",
        "admin_email": "testing_admin@example.com",
        "items_per_user": 50,
    }
