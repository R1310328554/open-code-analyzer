from django.db.models.expressions import Func
from django.db.models.fields import FloatField, IntegerField

__all__ = [
    "CumeDist",
    "DenseRank",
    "FirstValue",
    "Lag",
    "LastValue",
    "Lead",
    "NthValue",
    "Ntile",
    "PercentRank",
    "Rank",
    "RowNumber",
]

"""
django.db.models.functions.window — 窗口函数表达式。

排名、累计分布、LAG/LEAD、FIRST/LAST/NTH_VALUE 等。
"""

# 累计分布 CUME_DIST
class CumeDist(Func):]


class CumeDist(Func):
    function = "CUME_DIST"
    output_field = FloatField()
    window_compatible = True


# 密集排名 DENSE_RANK
class DenseRank(Func):
    function = "DENSE_RANK"
    output_field = IntegerField()
    window_compatible = True


# 窗口内第一个值 FIRST_VALUE
class FirstValue(Func):
    arity = 1
    function = "FIRST_VALUE"
    window_compatible = True


# LAG/LEAD 基类：offset 与 default 参数校验
class LagLeadFunction(Func):
    window_compatible = True

    def __init__(self, expression, offset=1, default=None, **extra):
        if expression is None:
            raise ValueError(
                "%s requires a non-null source expression." % self.__class__.__name__
            )
        if offset is None or offset <= 0:
            raise ValueError(
                "%s requires a positive integer for the offset."
                % self.__class__.__name__
            )
        args = (expression, offset)
        if default is not None:
            args += (default,)
        super().__init__(*args, **extra)

    def _resolve_output_field(self):
        sources = self.get_source_expressions()
        return sources[0].output_field


# 向前偏移 LAG
class Lag(LagLeadFunction):
    function = "LAG"


# 窗口内最后一个值 LAST_VALUE
class LastValue(Func):
    arity = 1
    function = "LAST_VALUE"
    window_compatible = True


# 向后偏移 LEAD
class Lead(LagLeadFunction):
    function = "LEAD"


# 窗口内第 n 个值 NTH_VALUE
class NthValue(Func):
    function = "NTH_VALUE"
    window_compatible = True

    def __init__(self, expression, nth=1, **extra):
        if expression is None:
            raise ValueError(
                "%s requires a non-null source expression." % self.__class__.__name__
            )
        if nth is None or nth <= 0:
            raise ValueError(
                "%s requires a positive integer as for nth." % self.__class__.__name__
            )
        super().__init__(expression, nth, **extra)

    def _resolve_output_field(self):
        sources = self.get_source_expressions()
        return sources[0].output_field


# 等分桶 NTILE
class Ntile(Func):
    function = "NTILE"
    output_field = IntegerField()
    window_compatible = True

    def __init__(self, num_buckets=1, **extra):
        if num_buckets <= 0:
            raise ValueError("num_buckets must be greater than 0.")
        super().__init__(num_buckets, **extra)


# 百分比排名 PERCENT_RANK
class PercentRank(Func):
    function = "PERCENT_RANK"
    output_field = FloatField()
    window_compatible = True


# 排名 RANK
class Rank(Func):
    function = "RANK"
    output_field = IntegerField()
    window_compatible = True


# 行号 ROW_NUMBER
class RowNumber(Func):
    function = "ROW_NUMBER"
    output_field = IntegerField()
    window_compatible = True
