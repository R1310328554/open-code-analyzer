from django.contrib.postgres.fields import ArrayField
from django.db.models import Subquery
from django.utils.functional import cached_property


# 将子查询结果包装为 PostgreSQL 数组表达式 ARRAY(...)
class ArraySubquery(Subquery):
    template = "ARRAY(%(subquery)s)"

    # 接受 QuerySet 并委托 Subquery 初始化
    def __init__(self, queryset, **kwargs):
        super().__init__(queryset, **kwargs)

    @cached_property
    # 输出为包裹子查询字段类型的 ArrayField
    def output_field(self):
        return ArrayField(self.query.output_field)
