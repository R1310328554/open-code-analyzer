"""教程 001：在 Pydantic v2 安装环境下从 pydantic.v1 导入 BaseModel（迁移过渡期写法）。"""

"""教程 001：在 Pydantic v2 安装环境下从 pydantic.v1 导入 BaseModel（迁移过渡期写法）。"""

from pydantic.v1 import BaseModel


class Item(BaseModel):
    """使用 pydantic.v1 子模块定义的物品模型。"""
    """使用 pydantic.v1 子模块定义的物品模型。"""
    name: str
    description: str | None = None
    size: float
