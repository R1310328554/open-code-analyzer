"""示例 01 配置：pydantic-settings BaseSettings 从环境变量/.env 加载，模块级单例 settings。"""

from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    """应用配置模型；字段名对应环境变量（不区分大小写），如 ADMIN_EMAIL。"""

    app_name: str = "Awesome API"  # 有默认值，未设置环境变量时使用
    admin_email: str  # 必填，须通过环境变量或 .env 提供
    items_per_user: int = 50


settings = Settings()  # 导入时即解析环境变量，全应用共享同一实例
