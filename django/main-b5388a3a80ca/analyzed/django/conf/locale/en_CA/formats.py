# This file is distributed under the same license as the Django package.
#
# The *_FORMAT strings use the Django date format syntax,
# see https://docs.djangoproject.com/en/dev/ref/templates/builtins/#date
# 加拿大英语日期、时间与数字本地化格式

# 完整日期显示格式
DATE_FORMAT = "j M Y"  # 25 Oct 2006
# 时间显示格式（12 小时制）
TIME_FORMAT = "P"  # 2:30 p.m.
# 日期时间组合显示格式
DATETIME_FORMAT = "j M Y, P"  # 25 Oct 2006, 2:30 p.m.
# 仅年月时的显示格式
YEAR_MONTH_FORMAT = "F Y"  # October 2006
# 仅月日时的显示格式
MONTH_DAY_FORMAT = "j F"  # 25 October
# 短日期格式（ISO 年-月-日）
SHORT_DATE_FORMAT = "Y-m-d"
# 短日期时间格式
SHORT_DATETIME_FORMAT = "Y-m-d P"
# 一周起始日：0 表示周日
FIRST_DAY_OF_WEEK = 0  # Sunday

# The *_INPUT_FORMATS strings use the Python strftime format syntax,
# see https://docs.python.org/library/datetime.html#strftime-strptime-behavior
# 表单日期输入接受的 strftime 格式列表
DATE_INPUT_FORMATS = [
    "%Y-%m-%d",  # '2006-05-15'
    "%y-%m-%d",  # '06-05-15'
]
# 表单日期时间输入格式
DATETIME_INPUT_FORMATS = [
    "%Y-%m-%d %H:%M:%S",  # '2006-05-15 14:30:57'
    "%y-%m-%d %H:%M:%S",  # '06-05-15 14:30:57'
    "%Y-%m-%d %H:%M:%S.%f",  # '2006-05-15 14:30:57.000200'
    "%y-%m-%d %H:%M:%S.%f",  # '06-05-15 14:30:57.000200'
    "%Y-%m-%d %H:%M",  # '2006-05-15 14:30'
    "%y-%m-%d %H:%M",  # '06-05-15 14:30'
]
# 小数分隔符
DECIMAL_SEPARATOR = "."
# 千位分隔符（非断行空格）
THOUSAND_SEPARATOR = " "  # non-breaking space
# 数字分组位数（千分位）
NUMBER_GROUPING = 3
