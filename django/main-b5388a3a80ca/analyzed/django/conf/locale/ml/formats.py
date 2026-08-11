# This file is distributed under the same license as the Django package.
#
# The *_FORMAT strings use the Django date format syntax,
# see https://docs.djangoproject.com/en/dev/ref/templates/builtins/#date
# 马拉雅拉姆语日期、时间与数字本地化格式
# 完整日期显示格式（月名 日, 年）
DATE_FORMAT = "N j, Y"
# 时间显示格式（本地化 12 小时制）
TIME_FORMAT = "P"
# 日期时间组合显示格式
DATETIME_FORMAT = "N j, Y, P"
# 仅年月时的显示格式
YEAR_MONTH_FORMAT = "F Y"
# 仅月日时的显示格式
MONTH_DAY_FORMAT = "F j"
# 短日期格式（月/日/年）
SHORT_DATE_FORMAT = "m/d/Y"
# 短日期时间格式
SHORT_DATETIME_FORMAT = "m/d/Y P"
# 一周起始日：0 表示周日
FIRST_DAY_OF_WEEK = 0  # Sunday

# The *_INPUT_FORMATS strings use the Python strftime format syntax,
# see https://docs.python.org/library/datetime.html#strftime-strptime-behavior
# 保留 ISO 格式在列表首位
# 表单日期输入接受的 strftime 格式列表
DATE_INPUT_FORMATS = [
    "%Y-%m-%d",  # '2006-10-25'
    "%m/%d/%Y",  # '10/25/2006'
    "%m/%d/%y",  # '10/25/06'
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
    "%m/%d/%Y %H:%M:%S",  # '10/25/2006 14:30:59'
    "%m/%d/%Y %H:%M:%S.%f",  # '10/25/2006 14:30:59.000200'
    "%m/%d/%Y %H:%M",  # '10/25/2006 14:30'
    "%m/%d/%y %H:%M:%S",  # '10/25/06 14:30:59'
    "%m/%d/%y %H:%M:%S.%f",  # '10/25/06 14:30:59.000200'
    "%m/%d/%y %H:%M",  # '10/25/06 14:30'
]
# 小数分隔符
DECIMAL_SEPARATOR = "."
# 千位分隔符
THOUSAND_SEPARATOR = ","
# 数字分组位数（千分位）
NUMBER_GROUPING = 3
