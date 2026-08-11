from django.conf import settings
from django.core import checks
from django.core.exceptions import FieldDoesNotExist
from django.db import models


# 管理器：将查询集限定为与当前 SITE_ID 关联的对象
class CurrentSiteManager(models.Manager):
    "Use this to limit objects to those associated with the current site."

    use_in_migrations = True

    # 可选指定外键或多对多字段名，默认自动探测 site 或 sites
    def __init__(self, field_name=None):
        super().__init__()
        self.__field_name = field_name

    # 校验关联字段存在且为 ForeignKey 或 ManyToManyField
    def check(self, **kwargs):
        errors = super().check(**kwargs)
        errors.extend(self._check_field_name())
        return errors

    # 返回 sites.E001/E002 错误或空列表
    def _check_field_name(self):
        field_name = self._get_field_name()
        try:
            field = self.model._meta.get_field(field_name)
        except FieldDoesNotExist:
            return [
                checks.Error(
                    "CurrentSiteManager could not find a field named '%s'."
                    % field_name,
                    obj=self,
                    id="sites.E001",
                )
            ]

        if not field.many_to_many and not isinstance(field, (models.ForeignKey)):
            return [
                checks.Error(
                    "CurrentSiteManager cannot use '%s.%s' as it is not a foreign key "
                    "or a many-to-many field."
                    % (self.model._meta.object_name, field_name),
                    obj=self,
                    id="sites.E002",
                )
            ]

        return []

    # 返回显式字段名，或按 site/sites 字段自动推断
    def _get_field_name(self):
        """Return self.__field_name or 'site' or 'sites'."""

        if not self.__field_name:
            try:
                self.model._meta.get_field("site")
            except FieldDoesNotExist:
                self.__field_name = "sites"
            else:
                self.__field_name = "site"
        return self.__field_name

    # 过滤关联字段 id 等于 settings.SITE_ID 的记录
    def get_queryset(self):
        return (
            super()
            .get_queryset()
            .filter(**{self._get_field_name() + "__id": settings.SITE_ID})
        )
