"""
django.forms — 表单验证与 HTML 渲染。

聚合 Field、Widget、Form、FormSet 与 ModelForm 公共 API。
"""
"""
Django validation and HTML form handling.
"""

from django.core.exceptions import ValidationError  # NOQA
from django.forms.boundfield import *  # NOQA
from django.forms.fields import *  # NOQA
from django.forms.forms import *  # NOQA
from django.forms.formsets import *  # NOQA
from django.forms.models import *  # NOQA
from django.forms.widgets import *  # NOQA
