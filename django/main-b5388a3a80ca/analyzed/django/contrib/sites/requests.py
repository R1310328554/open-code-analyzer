# 轻量站点对象：从 HttpRequest 取 Host，不依赖数据库
class RequestSite:
    """
    A class that shares the primary interface of Site (i.e., it has ``domain``
    and ``name`` attributes) but gets its data from an HttpRequest object
    rather than from a database.

    The save() and delete() methods raise NotImplementedError.
    """

    # domain 与 name 均设为 request.get_host()
    def __init__(self, request):
        self.domain = self.name = request.get_host()

    # 返回 domain
    def __str__(self):
        return self.domain

    # 不可持久化，调用时抛出 NotImplementedError
    def save(self, force_insert=False, force_update=False):
        raise NotImplementedError("RequestSite cannot be saved.")

    # 不可删除，调用时抛出 NotImplementedError
    def delete(self):
        raise NotImplementedError("RequestSite cannot be deleted.")
