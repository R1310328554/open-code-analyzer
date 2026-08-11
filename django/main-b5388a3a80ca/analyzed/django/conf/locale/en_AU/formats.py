# This file is distributed under the same license as the Django package.
#
# The *_FORMAT strings use the Django date format syntax,
# see https://docs.djangoproject.com/en/dev/ref/templates/builtins/#date
# 澳大利亚英语日期、时间与数字本地化格式
# 完整日期显示格式
DATE_FORMAT = "j M Y"  # '25 Oct 2006'
# 时间显示格式（12 小时制）
TIME_FORMAT = "P"  # '2:30 p.m.'
# 日期时间组合显示格式
DATETIME_FORMAT = "j M Y, P"  # '25 Oct 2006, 2:30 p.m.'
# 仅年月时的显示格式
YEAR_MONTH_FORMAT = "F Y"  # 'October 2006'
# 仅月日时的显示格式
MONTH_DAY_FORMAT = "j F"  # '25 October'
# 短日期格式（日/月/年）
SHORT_DATE_FORMAT = "d/m/Y"  # '25/10/2006'
# 短日期时间格式
SHORT_DATETIME_FORMAT = "d/m/Y P"  # '25/10/2006 2:30 p.m.'
# 一周起始日：0 表示周日
FIRST_DAY_OF_WEEK = 0  # Sunday

# The *_INPUT_FORMATS strings use the Python strftime format syntax,
# see https://docs.python.org/library/datetime.html#strftime-strptime-behavior
# 表单日期输入接受的 strftime 格式列表
DATE_INPUT_FORMATS = [
    "%d/%m/%Y",  # '25/10/2006'
    "%d/%m/%y",  # '25/10/06'
    # "%b %d %Y",  # 'Oct 25 2006'
    # "%b %d, %Y",  # 'Oct 25, 2006'
    # "%d %b %Y",  # '25 Oct 2006'
    # "%d %b, %Y",  # '25 Oct, 2006'
    # "%B %d %Y",  # 'October 25 2006'
    # "%B %d, %Y",  # 'October 25, 2006'
    # "%d %B %Y",  # '25 October 2006'
    # "%d %B, %Y",  # '25 October, 2006'
]
# 表单日期时间输入格式
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
# 小数分隔符
DECIMAL_SEPARATOR = "."
# 千位分隔符
THOUSAND_SEPARATOR = ","
# 数字分组位数（千分位）
NUMBER_GROUPING = 3
