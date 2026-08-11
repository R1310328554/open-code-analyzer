# This file is distributed under the same license as the Django package.
#
# The *_FORMAT strings use the Django date format syntax,
# see https://docs.djangoproject.com/en/dev/ref/templates/builtins/#date
# 英语（美国）日期、时间与数字本地化格式

# Formatting for date objects.
# 完整日期显示格式（月名 日, 年）
DATE_FORMAT = "N j, Y"
# Formatting for time objects.
# 时间显示格式（12 小时制）
TIME_FORMAT = "P"
# Formatting for datetime objects.
# 日期时间组合显示格式
DATETIME_FORMAT = "N j, Y, P"
# Formatting for date objects when only the year and month are relevant.
# 仅年月时的显示格式
YEAR_MONTH_FORMAT = "F Y"
# Formatting for date objects when only the month and day are relevant.
# 仅月日时的显示格式
MONTH_DAY_FORMAT = "F j"
# Short formatting for date objects.
# 短日期格式（月/日/年）
SHORT_DATE_FORMAT = "m/d/Y"
# Short formatting for datetime objects.
# 短日期时间格式
SHORT_DATETIME_FORMAT = "m/d/Y P"
# First day of week, to be used on calendars.
# 0 means Sunday, 1 means Monday...
# 一周起始日：0 表示周日
FIRST_DAY_OF_WEEK = 0

# Formats to be used when parsing dates from input boxes, in order.
# The *_INPUT_FORMATS strings use the Python strftime format syntax,
# see https://docs.python.org/library/datetime.html#strftime-strptime-behavior
# Note that these format strings are different from the ones to display dates.
# Kept ISO formats as they are in first position
# 表单日期输入格式；ISO 格式优先
DATE_INPUT_FORMATS = [
    "%Y-%m-%d",  # '2006-10-25'
    "%m/%d/%Y",  # '10/25/2006'
    "%m/%d/%y",  # '10/25/06'
    "%b %d %Y",  # 'Oct 25 2006'
    "%b %d, %Y",  # 'Oct 25, 2006'
    "%d %b %Y",  # '25 Oct 2006'
    "%d %b, %Y",  # '25 Oct, 2006'
    "%B %d %Y",  # 'October 25 2006'
    "%B %d, %Y",  # 'October 25, 2006'
    "%d %B %Y",  # '25 October 2006'
    "%d %B, %Y",  # '25 October, 2006'
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
# 表单时间输入格式
TIME_INPUT_FORMATS = [
    "%H:%M:%S",  # '14:30:59'
    "%H:%M:%S.%f",  # '14:30:59.000200'
    "%H:%M",  # '14:30'
]

# Decimal separator symbol.
# 小数分隔符
DECIMAL_SEPARATOR = "."
# Thousand separator symbol.
# 千位分隔符
THOUSAND_SEPARATOR = ","
# Number of digits that will be together, when splitting them by
# THOUSAND_SEPARATOR. 0 means no grouping, 3 means splitting by thousands.
# 数字分组位数（千分位）
NUMBER_GROUPING = 3
