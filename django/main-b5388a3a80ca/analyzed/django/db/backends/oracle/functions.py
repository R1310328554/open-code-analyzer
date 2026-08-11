"""
django.db.backends.oracle.functions — Oracle 时间间隔 SQL 函数。
"""
from django.db.models import DecimalField, DurationField, Func


# 将 INTERVAL DAY TO SECOND 转为秒数（DecimalField 输出）
class IntervalToSeconds(Func):
    function = ""
    template = """
    EXTRACT(day from %(expressions)s) * 86400 +
    EXTRACT(hour from %(expressions)s) * 3600 +
    EXTRACT(minute from %(expressions)s) * 60 +
    EXTRACT(second from %(expressions)s)
    """

    def __init__(self, expression, *, output_field=None, **extra):
        super().__init__(
            expression, output_field=output_field or DecimalField(), **extra
        )


# NUMTODSINTERVAL 将秒数转为 INTERVAL 类型
class SecondsToInterval(Func):
    function = "NUMTODSINTERVAL"
    template = "%(function)s(%(expressions)s, 'SECOND')"

    def __init__(self, expression, *, output_field=None, **extra):
        super().__init__(
            expression, output_field=output_field or DurationField(), **extra
        )
