from django.contrib.admin import ModelAdmin
from django.contrib.gis.db import models
from django.contrib.gis.forms import OSMWidget


# Admin 混入：为 GeometryField 自动选用 OSMWidget 地图控件
class GeoModelAdminMixin:
    gis_widget = OSMWidget
    gis_widget_kwargs = {}

    # 二维或 widget 支持三维几何时替换为 gis_widget，否则走父类逻辑
    def formfield_for_dbfield(self, db_field, request, **kwargs):
        if isinstance(db_field, models.GeometryField) and (
            db_field.dim < 3 or self.gis_widget.supports_3d
        ):
            kwargs["widget"] = self.gis_widget(**self.gis_widget_kwargs)
            return db_field.formfield(**kwargs)
        else:
            return super().formfield_for_dbfield(db_field, request, **kwargs)


# 带地图编辑能力的 ModelAdmin 基类
class GISModelAdmin(GeoModelAdminMixin, ModelAdmin):
    pass
