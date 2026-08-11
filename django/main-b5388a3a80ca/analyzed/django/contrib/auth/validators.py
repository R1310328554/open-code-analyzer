"""
django.contrib.auth.validators — 用户名字段正则校验器。

提供 ASCII 与 Unicode 两种 RegexValidator，用于 User.username。
"""
import re

from django.core import validators
from django.utils.deconstruct import deconstructible
from django.utils.translation import gettext_lazy as _


@deconstructible
# 仅允许未重音 ASCII 字母、数字及 @/./+/-/_
class ASCIIUsernameValidator(validators.RegexValidator):
    regex = r"^[\w.@+-]+\Z"
    message = _(
        "Enter a valid username. This value may contain only unaccented lowercase a-z "
        "and uppercase A-Z letters, numbers, and @/./+/-/_ characters."
    )
    flags = re.ASCII


@deconstructible
# 允许 Unicode 字母（\w）及 @/./+/-/_，Django 默认用户名规则
class UnicodeUsernameValidator(validators.RegexValidator):
    regex = r"^[\w.@+-]+\Z"
    message = _(
        "Enter a valid username. This value may contain only letters, "
        "numbers, and @/./+/-/_ characters."
    )
    flags = 0
