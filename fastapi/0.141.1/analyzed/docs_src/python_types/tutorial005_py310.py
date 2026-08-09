"""教程 005：声明标准 Python 简单类型——str、int、float、bool、bytes。"""


def get_items(item_a: str, item_b: int, item_c: float, item_d: bool, item_e: bytes):
    """演示多种内置类型的参数注解；FastAPI 路径/查询/body 参数同理可用。"""
    return item_a, item_b, item_c, item_d, item_e
