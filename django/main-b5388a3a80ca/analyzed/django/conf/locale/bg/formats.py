# This file is distributed under the same license as the Django package.
#
# The *_FORMAT strings use the Django date format syntax,
# see https://docs.djangoproject.com/en/dev/ref/templates/builtins/#date
# 保加利亚语日期、时间与数字本地化格式
# 完整日期显示格式
DATE_FORMAT = "d F Y"
TIME_FORMAT = "H:i"
# DATETIME_FORMAT =
# YEAR_MONTH_FORMAT =
MONTH_DAY_FORMAT = "j F"
SHORT_DATE_FORMAT = "d.m.Y"
# SHORT_DATETIME_FORMAT =
# FIRST_DAY_OF_WEEK =

# The *_INPUT_FORMATS strings use the Python strftime format syntax,
# see https://docs.python.org/library/datetime.html#strftime-strptime-behavior
# DATE_INPUT_FORMATS =
# TIME_INPUT_FORMATS =
# DATETIME_INPUT_FORMATS =
# 小数分隔符
DECIMAL_SEPARATOR = ","
# 千位分隔符（非断行空格）
THOUSAND_SEPARATOR = " "  # Non-breaking space
# NUMBER_GROUPING =
