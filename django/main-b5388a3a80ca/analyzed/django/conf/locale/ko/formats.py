# This file is distributed under the same license as the Django package.
#
# The *_FORMAT strings use the Django date format syntax,
# see https://docs.djangoproject.com/en/dev/ref/templates/builtins/#date
# 韩语日期、时间与数字本地化格式
# 完整日期显示格式（年 月 日，含韩语后缀）
DATE_FORMAT = "Y년 n월 j일"
# 时间显示格式（12 小时制，上午/下午在前）
TIME_FORMAT = "A g:i"
# 日期时间组合显示格式
DATETIME_FORMAT = "Y년 n월 j일 g:i A"
# 仅年月时的显示格式
YEAR_MONTH_FORMAT = "Y년 n월"
# 仅月日时的显示格式
MONTH_DAY_FORMAT = "n월 j일"
# 短日期格式（年-月-日）
SHORT_DATE_FORMAT = "Y-n-j"
# 短日期时间格式
SHORT_DATETIME_FORMAT = "Y-n-j H:i"
# 一周起始日（未覆盖）
# FIRST_DAY_OF_WEEK =

# The *_INPUT_FORMATS strings use the Python strftime format syntax,
# see https://docs.python.org/library/datetime.html#strftime-strptime-behavior
# Kept ISO formats as they are in first position
# 表单日期输入格式；ISO 格式优先
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
    "%Y년 %m월 %d일",  # '2006년 10월 25일', with localized suffix.
]
# 表单时间输入格式
TIME_INPUT_FORMATS = [
    "%H:%M:%S",  # '14:30:59'
    "%H:%M:%S.%f",  # '14:30:59.000200'
    "%H:%M",  # '14:30'
    "%H시 %M분 %S초",  # '14시 30분 59초'
    "%H시 %M분",  # '14시 30분'
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
    "%Y년 %m월 %d일 %H시 %M분 %S초",  # '2006년 10월 25일 14시 30분 59초'
    "%Y년 %m월 %d일 %H시 %M분",  # '2006년 10월 25일 14시 30분'
]

# 小数分隔符
DECIMAL_SEPARATOR = "."
# 千位分隔符
THOUSAND_SEPARATOR = ","
# 数字分组位数（千分位）
NUMBER_GROUPING = 3
