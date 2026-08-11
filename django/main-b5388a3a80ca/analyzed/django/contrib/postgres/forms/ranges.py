from django import forms
from django.core import exceptions
from django.db.backends.postgresql.psycopg_any import (
    DateRange,
    DateTimeTZRange,
    NumericRange,
)
from django.forms.widgets import HiddenInput, MultiWidget
from django.utils.translation import gettext_lazy as _

__all__ = [
    "BaseRangeField",
    "IntegerRangeField",
    "DecimalRangeField",
    "DateTimeRangeField",
    "DateRangeField",
    "HiddenRangeWidget",
    "RangeWidget",
]


# 范围控件 — 用两个相同子控件分别编辑下界与上界
class RangeWidget(MultiWidget):
    def __init__(self, base_widget, attrs=None):
        widgets = (base_widget, base_widget)
        super().__init__(widgets, attrs)

    # 将 range 对象拆为 (lower, upper) 元组
    def decompress(self, value):
        if value:
            return (value.lower, value.upper)
        return (None, None)


# 隐藏范围控件 — 两个 hidden input 承载范围上下界
class HiddenRangeWidget(RangeWidget):
    """A widget that splits input into two <input type="hidden"> inputs."""

    def __init__(self, attrs=None):
        super().__init__(HiddenInput, attrs)


# 范围字段基类 — 组合两个子字段并压缩为 PostgreSQL range 类型
class BaseRangeField(forms.MultiValueField):
    default_error_messages = {
        "invalid": _("Enter two valid values."),
        "bound_ordering": _(
            "The start of the range must not exceed the end of the range."
        ),
    }
    hidden_widget = HiddenRangeWidget

    def __init__(self, **kwargs):
        if "widget" not in kwargs:
            kwargs["widget"] = RangeWidget(self.base_field.widget)
        if "fields" not in kwargs:
            kwargs["fields"] = [
                self.base_field(required=False),
                self.base_field(required=False),
            ]
        kwargs.setdefault("required", False)
        kwargs.setdefault("require_all_fields", False)
        self.range_kwargs = {}
        if default_bounds := kwargs.pop("default_bounds", None):
            self.range_kwargs = {"bounds": default_bounds}
        super().__init__(**kwargs)

    def prepare_value(self, value):
        lower_base, upper_base = self.fields
        if isinstance(value, self.range_type):
            return [
                lower_base.prepare_value(value.lower),
                upper_base.prepare_value(value.upper),
            ]
        if value is None:
            return [
                lower_base.prepare_value(None),
                upper_base.prepare_value(None),
            ]
        return value

    # 校验上下界顺序后构造 range_type 实例
    def compress(self, values):
        if not values:
            return None
        lower, upper = values
        if lower is not None and upper is not None and lower > upper:
            raise exceptions.ValidationError(
                self.error_messages["bound_ordering"],
                code="bound_ordering",
            )
        try:
            range_value = self.range_type(lower, upper, **self.range_kwargs)
        except TypeError:
            raise exceptions.ValidationError(
                self.error_messages["invalid"],
                code="invalid",
            )
        else:
            return range_value


# 整数范围字段（NumericRange）
class IntegerRangeField(BaseRangeField):
    default_error_messages = {"invalid": _("Enter two whole numbers.")}
    base_field = forms.IntegerField
    range_type = NumericRange


# 小数范围字段（NumericRange）
class DecimalRangeField(BaseRangeField):
    default_error_messages = {"invalid": _("Enter two numbers.")}
    base_field = forms.DecimalField
    range_type = NumericRange


# 带时区日期时间范围字段（DateTimeTZRange）
class DateTimeRangeField(BaseRangeField):
    default_error_messages = {"invalid": _("Enter two valid date/times.")}
    base_field = forms.DateTimeField
    range_type = DateTimeTZRange


# 日期范围字段（DateRange）
class DateRangeField(BaseRangeField):
    default_error_messages = {"invalid": _("Enter two valid dates.")}
    base_field = forms.DateField
    range_type = DateRange
