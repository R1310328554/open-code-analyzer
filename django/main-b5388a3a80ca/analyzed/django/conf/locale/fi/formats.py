# This file is distributed under the same license as the Django package.
#
# The *_FORMAT strings use the Django date format syntax,
# see https://docs.djangoproject.com/en/dev/ref/templates/builtins/#date
# 芬兰语日期、时间与数字本地化格式
# 完整日期显示格式（日. 星期缩写 年）
DATE_FORMAT = "j. E Y"
# 时间显示格式（24 小时制，点号分隔）
TIME_FORMAT = "G.i"
# 日期时间组合显示格式（含 kello 表示「在…点」）
DATETIME_FORMAT = r"j. E Y \k\e\l\l\o G.i"
# 仅年月时的显示格式
YEAR_MONTH_FORMAT = "F Y"
# 仅月日时的显示格式
MONTH_DAY_FORMAT = "j. F"
# 短日期格式（日.月.年）
SHORT_DATE_FORMAT = "j.n.Y"
# 短日期时间格式
SHORT_DATETIME_FORMAT = "j.n.Y G.i"
# 一周起始日：1 表示周一
FIRST_DAY_OF_WEEK = 1  # Monday

# The *_INPUT_FORMATS strings use the Python strftime format syntax,
# see https://docs.python.org/library/datetime.html#strftime-strptime-behavior
# 表单日期输入接受的 strftime 格式列表
DATE_INPUT_FORMATS = [
    "%d.%m.%Y",  # '20.3.2014'
    "%d.%m.%y",  # '20.3.14'
]
# 表单日期时间输入格式（时间以点号分隔）
DATETIME_INPUT_FORMATS = [
    "%d.%m.%Y %H.%M.%S",  # '20.3.2014 14.30.59'
    "%d.%m.%Y %H.%M.%S.%f",  # '20.3.2014 14.30.59.000200'
    "%d.%m.%Y %H.%M",  # '20.3.2014 14.30'
    "%d.%m.%y %H.%M.%S",  # '20.3.14 14.30.59'
    "%d.%m.%y %H.%M.%S.%f",  # '20.3.14 14.30.59.000200'
    "%d.%m.%y %H.%M",  # '20.3.14 14.30'
]
# 表单时间输入格式
TIME_INPUT_FORMATS = [
    "%H.%M.%S",  # '14.30.59'
    "%H.%M.%S.%f",  # '14.30.59.000200'
    "%H.%M",  # '14.30'
]
# 小数分隔符
DECIMAL_SEPARATOR = ","
# 千位分隔符（非断行空格）
THOUSAND_SEPARATOR = "\xa0"  # Non-breaking space
# 数字分组位数（千分位）
NUMBER_GROUPING = 3
