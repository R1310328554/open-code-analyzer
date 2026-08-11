from django.db.models import Aggregate, FloatField, IntegerField

__all__ = [
    "CovarPop",
    "Corr",
    "RegrAvgX",
    "RegrAvgY",
    "RegrCount",
    "RegrIntercept",
    "RegrR2",
    "RegrSlope",
    "RegrSXX",
    "RegrSXY",
    "RegrSYY",
    "StatAggregate",
]


# 双变量统计聚合基类：要求同时提供 y 与 x 表达式
class StatAggregate(Aggregate):
    output_field = FloatField()

    # y、x 均不能为空，否则抛出 ValueError
    def __init__(self, y, x, output_field=None, filter=None, default=None):
        if not x or not y:
            raise ValueError("Both y and x must be provided.")
        super().__init__(
            y, x, output_field=output_field, filter=filter, default=default
        )


# 皮尔逊相关系数（CORR）
class Corr(StatAggregate):
    function = "CORR"


# 总体或样本协方差（COVAR_POP / COVAR_SAMP）
class CovarPop(StatAggregate):
    # sample=True 时使用 COVAR_SAMP，否则 COVAR_POP
    def __init__(self, y, x, sample=False, filter=None, default=None):
        self.function = "COVAR_SAMP" if sample else "COVAR_POP"
        super().__init__(y, x, filter=filter, default=default)


# 线性回归自变量均值（REGR_AVGX）
class RegrAvgX(StatAggregate):
    function = "REGR_AVGX"


# 线性回归因变量均值（REGR_AVGY）
class RegrAvgY(StatAggregate):
    function = "REGR_AVGY"


# 参与回归的非空行计数（REGR_COUNT）
class RegrCount(StatAggregate):
    function = "REGR_COUNT"
    output_field = IntegerField()
    empty_result_set_value = 0


# 线性回归截距（REGR_INTERCEPT）
class RegrIntercept(StatAggregate):
    function = "REGR_INTERCEPT"


# 决定系数 R²（REGR_R2）
class RegrR2(StatAggregate):
    function = "REGR_R2"


# 回归斜率（REGR_SLOPE）
class RegrSlope(StatAggregate):
    function = "REGR_SLOPE"


# 自变量平方和（REGR_SXX）
class RegrSXX(StatAggregate):
    function = "REGR_SXX"


# 自变量与因变量乘积和（REGR_SXY）
class RegrSXY(StatAggregate):
    function = "REGR_SXY"


# 因变量平方和（REGR_SYY）
class RegrSYY(StatAggregate):
    function = "REGR_SYY"
