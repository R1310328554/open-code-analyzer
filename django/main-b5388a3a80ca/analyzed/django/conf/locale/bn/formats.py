# This file is distributed under the same license as the Django package.
#
# The *_FORMAT strings use the Django date format syntax,
# see https://docs.djangoproject.com/en/dev/ref/templates/builtins/#date
# 孟加拉语日期、时间与数字本地化格式
# 完整日期显示格式
DATE_FORMAT = "j F, Y"
TIME_FORMAT = "g:i A"
# DATETIME_FORMAT =
YEAR_MONTH_FORMAT = "F Y"
MONTH_DAY_FORMAT = "j F"
SHORT_DATE_FORMAT = "j M, Y"
# SHORT_DATETIME_FORMAT =
# 一周起始日：6 表示周六
FIRST_DAY_OF_WEEK = 6  # Saturday

# The *_INPUT_FORMATS strings use the Python strftime format syntax,
# see https://docs.python.org/library/datetime.html#strftime-strptime-behavior
# 表单日期输入接受的 strftime 格式列表
DATE_INPUT_FORMATS = [
    "%d/%m/%Y",  # 25/10/2016
    "%d/%m/%y",  # 25/10/16
    "%d-%m-%Y",  # 25-10-2016
    "%d-%m-%y",  # 25-10-16
]
# 时间输入格式
TIME_INPUT_FORMATS = [
    "%H:%M:%S",  # 14:30:59
    "%H:%M",  # 14:30
]
# 日期时间输入格式
DATETIME_INPUT_FORMATS = [
    "%d/%m/%Y %H:%M:%S",  # 25/10/2006 14:30:59
    "%d/%m/%Y %H:%M",  # 25/10/2006 14:30
]
DECIMAL_SEPARATOR = "."
THOUSAND_SEPARATOR = ","
# NUMBER_GROUPING =
