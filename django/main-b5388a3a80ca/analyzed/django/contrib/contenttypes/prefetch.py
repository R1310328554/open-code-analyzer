"""
django.contrib.contenttypes.prefetch — 通用外键批量预取。

GenericPrefetch 为 GenericForeignKey 反向关系提供多 queryset 预取支持。
"""
from django.db.models import Prefetch
from django.db.models.query import ModelIterable, RawQuerySet


# 扩展 Prefetch：为 GFK 反向路径绑定多个可选 queryset
class GenericPrefetch(Prefetch):
    # 校验 querysets 不得使用 raw/values/values_list
    def __init__(self, lookup, querysets, to_attr=None):
        for queryset in querysets:
            if queryset is not None and (
                isinstance(queryset, RawQuerySet)
                or (
                    hasattr(queryset, "_iterable_class")
                    and not issubclass(queryset._iterable_class, ModelIterable)
                )
            ):
                raise ValueError(
                    "Prefetch querysets cannot use raw(), values(), and values_list()."
                )
        self.querysets = querysets
        super().__init__(lookup, to_attr=to_attr)

    # pickle 时链式复制 queryset 并标记 _prefetch_done 防提前求值
    def __getstate__(self):
        obj_dict = self.__dict__.copy()
        obj_dict["querysets"] = []
        for queryset in self.querysets:
            if queryset is not None:
                queryset = queryset._chain()
                # Prevent the QuerySet from being evaluated
                queryset._result_cache = []
                queryset._prefetch_done = True
                obj_dict["querysets"].append(queryset)
        return obj_dict

    # 仅在 prefetch_to 匹配当前层级时返回绑定的 querysets
    def get_current_querysets(self, level):
        if self.get_current_prefetch_to(level) == self.prefetch_to:
            return self.querysets
        return None
