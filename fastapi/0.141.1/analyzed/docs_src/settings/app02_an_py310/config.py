"""示例 02 配置类：仅定义 Settings，不在模块级实例化——由 get_settings 依赖按需创建。"""

from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    """与 app01 相同字段；admin_email 必填，其余有默认值。"""

    app_name: str = "Awesome API"
    admin_email: str
    items_per_user: int = 50
