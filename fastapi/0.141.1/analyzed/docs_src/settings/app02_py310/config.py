"""示例 02 配置：Settings 类定义，供 get_settings 依赖函数实例化。"""

from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    """BaseSettings 自动读取环境变量；字段 admin_email 无默认值，部署时必须配置。"""

    app_name: str = "Awesome API"
    admin_email: str
    items_per_user: int = 50
