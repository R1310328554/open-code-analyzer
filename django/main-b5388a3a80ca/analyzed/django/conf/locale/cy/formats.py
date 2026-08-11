# This file is distributed under the same license as the Django package.
#
# The *_FORMAT strings use the Django date format syntax,
# see https://docs.djangoproject.com/en/dev/ref/templates/builtins/#date
# 威尔士语日期、时间与数字本地化格式
# 完整日期显示格式
DATE_FORMAT = "j F Y"  # '25 Hydref 2006'
TIME_FORMAT = "P"  # '2:30 y.b.'
DATETIME_FORMAT = "j F Y, P"  # '25 Hydref 2006, 2:30 y.b.'
YEAR_MONTH_FORMAT = "F Y"  # 'Hydref 2006'
MONTH_DAY_FORMAT = "j F"  # '25 Hydref'
SHORT_DATE_FORMAT = "d/m/Y"  # '25/10/2006'
SHORT_DATETIME_FORMAT = "d/m/Y P"  # '25/10/2006 2:30 y.b.'
# 一周起始日：1 表示周一（Dydd Llun）
FIRST_DAY_OF_WEEK = 1  # 'Dydd Llun'

# The *_INPUT_FORMATS strings use the Python strftime format syntax,
# see https://docs.python.org/library/datetime.html#strftime-strptime-behavior
# 表单日期输入接受的 strftime 格式列表
DATE_INPUT_FORMATS = [
    "%d/%m/%Y",  # '25/10/2006'
    "%d/%m/%y",  # '25/10/06'
]
DATETIME_INPUT_FORMATS = [
    "%Y-%m-%d %H:%M:%S",  # '2006-10-25 14:30:59'
    "%Y-%m-%d %H:%M:%S.%f",  # '2006-10-25 14:30:59.000200'
    "%Y-%m-%d %H:%M",  # '2006-10-25 14:30'
    "%d/%m/%Y %H:%M:%S",  # '25/10/2006 14:30:59'
    "%d/%m/%Y %H:%M:%S.%f",  # '25/10/2006 14:30:59.000200'
    "%d/%m/%Y %H:%M",  # '25/10/2006 14:30'
    "%d/%m/%y %H:%M:%S",  # '25/10/06 14:30:59'
    "%d/%m/%y %H:%M:%S.%f",  # '25/10/06 14:30:59.000200'
    "%d/%m/%y %H:%M",  # '25/10/06 14:30'
]
DECIMAL_SEPARATOR = "."
THOUSAND_SEPARATOR = ","
# 数字分组位数（千分位）
NUMBER_GROUPING = 3
