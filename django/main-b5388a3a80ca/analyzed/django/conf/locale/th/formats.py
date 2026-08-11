# This file is distributed under the same license as the Django package.
#
# The *_FORMAT strings use the Django date format syntax,
# see https://docs.djangoproject.com/en/dev/ref/templates/builtins/#date
# 泰语日期、时间与数字本地化格式
# 完整日期显示格式（日 月名 年）
DATE_FORMAT = "j F Y"
# 时间显示格式（24 小时制，无前导零小时）
TIME_FORMAT = "G:i"
# 日期时间组合显示格式
DATETIME_FORMAT = "j F Y, G:i"
# 仅年月时的显示格式
YEAR_MONTH_FORMAT = "F Y"
# 仅月日时的显示格式
MONTH_DAY_FORMAT = "j F"
# 短日期格式（日 月缩写 年）
SHORT_DATE_FORMAT = "j M Y"
# 短日期时间格式
SHORT_DATETIME_FORMAT = "j M Y, G:i"
# 一周起始日：0 表示周日
FIRST_DAY_OF_WEEK = 0  # Sunday

# The *_INPUT_FORMATS strings use the Python strftime format syntax,
# see https://docs.python.org/library/datetime.html#strftime-strptime-behavior
# 表单日期输入接受的 strftime 格式列表
DATE_INPUT_FORMATS = [
    "%d/%m/%Y",  # 25/10/2006
    "%d %b %Y",  # 25 ต.ค. 2006
    "%d %B %Y",  # 25 ตุลาคม 2006
]
# 表单时间输入格式
TIME_INPUT_FORMATS = [
    "%H:%M:%S",  # 14:30:59
    "%H:%M:%S.%f",  # 14:30:59.000200
    "%H:%M",  # 14:30
]
# 表单日期时间输入格式
DATETIME_INPUT_FORMATS = [
    "%d/%m/%Y %H:%M:%S",  # 25/10/2006 14:30:59
    "%d/%m/%Y %H:%M:%S.%f",  # 25/10/2006 14:30:59.000200
    "%d/%m/%Y %H:%M",  # 25/10/2006 14:30
]
# 小数分隔符
DECIMAL_SEPARATOR = "."
# 千位分隔符
THOUSAND_SEPARATOR = ","
# 数字分组位数（千分位）
NUMBER_GROUPING = 3
