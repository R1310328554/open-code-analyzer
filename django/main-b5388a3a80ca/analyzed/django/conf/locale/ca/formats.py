# This file is distributed under the same license as the Django package.
#
# The *_FORMAT strings use the Django date format syntax,
# see https://docs.djangoproject.com/en/dev/ref/templates/builtins/#date
# 加泰罗尼亚语日期、时间与数字本地化格式
# 完整日期显示格式（含转义字面量 de/les）
DATE_FORMAT = r"j E \d\e Y"
TIME_FORMAT = "G:i"
DATETIME_FORMAT = r"j E \d\e Y \a \l\e\s G:i"
YEAR_MONTH_FORMAT = r"F \d\e\l Y"
MONTH_DAY_FORMAT = r"j E"
SHORT_DATE_FORMAT = "d/m/Y"
SHORT_DATETIME_FORMAT = "d/m/Y G:i"
# 一周起始日：1 表示周一
FIRST_DAY_OF_WEEK = 1  # Monday

# The *_INPUT_FORMATS strings use the Python strftime format syntax,
# see https://docs.python.org/library/datetime.html#strftime-strptime-behavior
# 表单日期输入接受的 strftime 格式列表
DATE_INPUT_FORMATS = [
    "%d/%m/%Y",  # '31/12/2009'
    "%d/%m/%y",  # '31/12/09'
]
DATETIME_INPUT_FORMATS = [
    "%d/%m/%Y %H:%M:%S",
    "%d/%m/%Y %H:%M:%S.%f",
    "%d/%m/%Y %H:%M",
    "%d/%m/%y %H:%M:%S",
    "%d/%m/%y %H:%M:%S.%f",
    "%d/%m/%y %H:%M",
]
DECIMAL_SEPARATOR = ","
THOUSAND_SEPARATOR = "."
# 数字分组位数（千分位）
NUMBER_GROUPING = 3
