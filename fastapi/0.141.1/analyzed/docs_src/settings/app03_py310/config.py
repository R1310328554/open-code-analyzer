"""app03 配置模块：Settings 从 .env 文件与环境变量加载。"""

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    """应用配置；admin_email 无默认值，必须在 .env 或环境中提供。"""

    app_name: str = "Awesome API"
    admin_email: str
    items_per_user: int = 50

    model_config = SettingsConfigDict(env_file=".env")  # 自动读取项目根 .env
