"""教程 007：yield 依赖——请求结束后在 finally 中关闭资源（如数据库会话）。"""


async def get_db():
    """生成器依赖：yield 前创建资源，yield 后执行 cleanup。"""
    db = DBSession()
    try:
        yield db
    finally:
        db.close()
