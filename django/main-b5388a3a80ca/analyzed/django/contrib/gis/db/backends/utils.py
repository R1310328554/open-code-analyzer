"""
空间后端通用工具：GIS 查找运算符的 SQL 模板封装。

A collection of utility routines and classes used by the spatial
backends.
""""""
A collection of utility routines and classes used by the spatial
backends.
"""


# GIS 查找运算符：根据 op/func 生成 lhs op rhs 或 func(lhs, rhs) SQL
class SpatialOperator:
    """
    Class encapsulating the behavior specific to a GIS operation (used by
    lookups).
    """

    sql_template = None

    def __init__(self, op=None, func=None):
        self.op = op
        self.func = func

    @property
    def default_template(self):
        if self.func:
            return "%(func)s(%(lhs)s, %(rhs)s)"
        else:
            return "%(lhs)s %(op)s %(rhs)s"

    # 合并模板参数并渲染最终 SQL 与绑定参数
    def as_sql(self, connection, lookup, template_params, sql_params):
        sql_template = self.sql_template or lookup.sql_template or self.default_template
        template_params.update({"op": self.op, "func": self.func})
        return sql_template % template_params, sql_params
