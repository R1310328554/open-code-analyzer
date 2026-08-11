# This file is distributed under the same license as the Django package.
#
# The *_FORMAT strings use the Django date format syntax,
# see https://docs.djangoproject.com/en/dev/ref/templates/builtins/#date
# 瑞士德语日期、时间与数字本地化格式
# 完整日期显示格式（日. 月名 年）
DATE_FORMAT = "j. F Y"
# 时间显示格式（24 小时制）
TIME_FORMAT = "H:i"
# 日期时间组合显示格式
DATETIME_FORMAT = "j. F Y H:i"
# 仅年月时的显示格式
YEAR_MONTH_FORMAT = "F Y"
# 仅月日时的显示格式
MONTH_DAY_FORMAT = "j. F"
# 短日期格式（日.月.年）
SHORT_DATE_FORMAT = "d.m.Y"
# 短日期时间格式
SHORT_DATETIME_FORMAT = "d.m.Y H:i"
# 一周起始日：1 表示周一
FIRST_DAY_OF_WEEK = 1  # Monday

# The *_INPUT_FORMATS strings use the Python strftime format syntax,
# see https://docs.python.org/library/datetime.html#strftime-strptime-behavior
# 表单日期输入接受的 strftime 格式列表
DATE_INPUT_FORMATS = [
    "%d.%m.%Y",  # '25.10.2006'
    "%d.%m.%y",  # '25.10.06'
    # "%d. %B %Y",  # '25. October 2006'
    # "%d. %b. %Y",  # '25. Oct. 2006'
]
# 表单日期时间输入格式
DATETIME_INPUT_FORMATS = [
    "%d.%m.%Y %H:%M:%S",  # '25.10.2006 14:30:59'
    "%d.%m.%Y %H:%M:%S.%f",  # '25.10.2006 14:30:59.000200'
    "%d.%m.%Y %H:%M",  # '25.10.2006 14:30'
]

# Swiss number formatting can vary based on context (e.g. Fr. 23.50 vs 22,5 m).
# Django does not support context-specific formatting and uses generic
# separators.
# 瑞士数字格式因语境而异；Django 使用通用分隔符
# 小数分隔符
DECIMAL_SEPARATOR = ","
# 千位分隔符（非断行空格）
THOUSAND_SEPARATOR = " "  # non-breaking space
# 数字分组位数（千分位）
NUMBER_GROUPING = 3
