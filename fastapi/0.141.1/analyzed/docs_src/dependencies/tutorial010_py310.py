"""教程 010：在 yield 依赖中使用同步上下文管理器（with ... as db）。"""


class MySuperContextManager:
    """包装 DBSession 的同步上下文管理器，__exit__ 中关闭连接。"""

    def __init__(self):
        self.db = DBSession()

    def __enter__(self):
        return self.db

    def __exit__(self, exc_type, exc_value, traceback):
        self.db.close()


async def get_db():
    """with 块结束后 __exit__ 自动关闭 db；yield 仍向路径函数提供会话。"""
    with MySuperContextManager() as db:
        yield db
