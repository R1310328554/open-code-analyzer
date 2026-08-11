# RemovedInDjango70Warning: When the deprecation ends, remove completely.
import warnings

from django.utils.deprecation import RemovedInDjango70Warning


# RemovedInDjango70Warning.
# 已弃用：为聚合启用 ORDER BY 的混入，请直接使用 Aggregate.allow_order_by
class OrderableAggMixin:
    allow_order_by = True

    # 子类化时发出 RemovedInDjango70Warning
    def __init_subclass__(cls, /, *args, **kwargs):
        warnings.warn(
            "OrderableAggMixin is deprecated. Use Aggregate and allow_order_by "
            "instead.",
            category=RemovedInDjango70Warning,
            stacklevel=1,
        )
        super().__init_subclass__(*args, **kwargs)
