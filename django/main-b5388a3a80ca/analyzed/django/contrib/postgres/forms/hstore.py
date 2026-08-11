import json

from django import forms
from django.core.exceptions import ValidationError
from django.utils.translation import gettext_lazy as _

__all__ = ["HStoreField"]


# HStore 表单字段 — 接受 JSON 字典输入并规范化为字符串键值
class HStoreField(forms.CharField):
    """
    A field for HStore data which accepts dictionary JSON input.
    """

    widget = forms.Textarea
    default_error_messages = {
        "invalid_json": _("Could not load JSON data."),
        "invalid_format": _("Input must be a JSON dictionary."),
    }

    # 字典序列化为 JSON 字符串供 textarea 展示
    def prepare_value(self, value):
        if isinstance(value, dict):
            return json.dumps(value, ensure_ascii=False)
        return value

    # 解析 JSON 并确保结果为 dict，非空值转为 str
    def to_python(self, value):
        if not value:
            return {}
        if not isinstance(value, dict):
            try:
                value = json.loads(value)
            except json.JSONDecodeError:
                raise ValidationError(
                    self.error_messages["invalid_json"],
                    code="invalid_json",
                )

        if not isinstance(value, dict):
            raise ValidationError(
                self.error_messages["invalid_format"],
                code="invalid_format",
            )

        # Cast everything to strings for ease.
        for key, val in value.items():
            if val is not None:
                val = str(val)
            value[key] = val
        return value

    def has_changed(self, initial, data):
        """
        Return True if data differs from initial.
        """
        # For purposes of seeing whether something has changed, None is
        # the same as an empty dict, if the data or initial value we get
        # is None, replace it w/ {}.
        initial_value = self.to_python(initial)
        return super().has_changed(initial_value, data)
