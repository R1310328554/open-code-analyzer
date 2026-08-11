# This file is distributed under the same license as the Django package.
#
# The *_FORMAT strings use the Django date format syntax,
# see https://docs.djangoproject.com/en/dev/ref/templates/builtins/#date
# 乌兹别克语日期、时间与数字本地化格式
# 完整日期显示格式（日-月名, 年-yil）
DATE_FORMAT = r"j-E, Y-\y\i\l"
# 时间显示格式（24 小时制）
TIME_FORMAT = "G:i"
# 日期时间组合显示格式
DATETIME_FORMAT = r"j-E, Y-\y\i\l G:i"
# 仅年月时的显示格式
YEAR_MONTH_FORMAT = r"F Y-\y\i\l"
# 仅月日时的显示格式
MONTH_DAY_FORMAT = "j-E"
# 短日期格式（日.月.年）
SHORT_DATE_FORMAT = "d.m.Y"
# 短日期时间格式
SHORT_DATETIME_FORMAT = "d.m.Y H:i"
# 一周起始日：1 表示周一
FIRST_DAY_OF_WEEK = 1  # Monday

# The *_INPUT_FORMATS strings use the Python strftime format syntax,
# see https://docs.python.org/library/datetime.html#strftime-strptime-behavior
# 表单日期输入格式
DATE_INPUT_FORMATS = [
    "%d.%m.%Y",  # '25.10.2006'
    "%d-%B, %Y-yil",  # '25-Oktabr, 2006-yil'
]
# 表单日期时间输入格式
DATETIME_INPUT_FORMATS = [
    "%d.%m.%Y %H:%M:%S",  # '25.10.2006 14:30:59'
    "%d.%m.%Y %H:%M:%S.%f",  # '25.10.2006 14:30:59.000200'
    "%d.%m.%Y %H:%M",  # '25.10.2006 14:30'
    "%d-%B, %Y-yil %H:%M:%S",  # '25-Oktabr, 2006-yil 14:30:59'
    "%d-%B, %Y-yil %H:%M:%S.%f",  # '25-Oktabr, 2006-yil 14:30:59.000200'
    "%d-%B, %Y-yil %H:%M",  # '25-Oktabr, 2006-yil 14:30'
]
# 小数分隔符
DECIMAL_SEPARATOR = ","
# 千位分隔符（非断行空格）
THOUSAND_SEPARATOR = "\xa0"  # non-breaking space
# 数字分组位数（千分位）
NUMBER_GROUPING = 3
