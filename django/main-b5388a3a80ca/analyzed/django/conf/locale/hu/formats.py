# This file is distributed under the same license as the Django package.
#
# The *_FORMAT strings use the Django date format syntax,
# see https://docs.djangoproject.com/en/dev/ref/templates/builtins/#date
# 匈牙利语日期、时间与数字本地化格式
# 完整日期显示格式（年. 月名 日.）
DATE_FORMAT = "Y. F j."
# 时间显示格式（24 小时制）
TIME_FORMAT = "H:i"
# 日期时间组合显示格式
DATETIME_FORMAT = "Y. F j. H:i"
# 仅年月时的显示格式
YEAR_MONTH_FORMAT = "Y. F"
# 仅月日时的显示格式
MONTH_DAY_FORMAT = "F j."
# 短日期格式（年.月.日.）
SHORT_DATE_FORMAT = "Y.m.d."
# 短日期时间格式
SHORT_DATETIME_FORMAT = "Y.m.d. H:i"
# 一周起始日：1 表示周一
FIRST_DAY_OF_WEEK = 1  # Monday

# The *_INPUT_FORMATS strings use the Python strftime format syntax,
# see https://docs.python.org/library/datetime.html#strftime-strptime-behavior
# 表单日期输入接受的 strftime 格式列表
DATE_INPUT_FORMATS = [
    "%Y.%m.%d.",  # '2006.10.25.'
]
# 表单时间输入格式
TIME_INPUT_FORMATS = [
    "%H:%M:%S",  # '14:30:59'
    "%H:%M",  # '14:30'
]
# 表单日期时间输入格式
DATETIME_INPUT_FORMATS = [
    "%Y.%m.%d. %H:%M:%S",  # '2006.10.25. 14:30:59'
    "%Y.%m.%d. %H:%M:%S.%f",  # '2006.10.25. 14:30:59.000200'
    "%Y.%m.%d. %H:%M",  # '2006.10.25. 14:30'
]
# 小数分隔符
DECIMAL_SEPARATOR = ","
# 千位分隔符（非断行空格）
THOUSAND_SEPARATOR = "\xa0"  # Non-breaking space
# 数字分组位数（千分位）
NUMBER_GROUPING = 3
