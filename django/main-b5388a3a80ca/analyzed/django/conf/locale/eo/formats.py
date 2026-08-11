# This file is distributed under the same license as the Django package.
#
# The *_FORMAT strings use the Django date format syntax,
# see https://docs.djangoproject.com/en/dev/ref/templates/builtins/#date
# 世界语日期、时间与数字本地化格式
# 完整日期显示格式（序数词 de 月名 年）
DATE_FORMAT = r"j\-\a \d\e F Y"  # '26-a de julio 1887'
# 时间显示格式（24 小时制）
TIME_FORMAT = "H:i"  # '18:59'
# 日期时间组合显示格式
DATETIME_FORMAT = r"j\-\a \d\e F Y\, \j\e H:i"  # '26-a de julio 1887, je 18:59'
# 仅年月时的显示格式
YEAR_MONTH_FORMAT = r"F \d\e Y"  # 'julio de 1887'
# 仅月日时的显示格式
MONTH_DAY_FORMAT = r"j\-\a \d\e F"  # '26-a de julio'
# 短日期格式（ISO 年-月-日）
SHORT_DATE_FORMAT = "Y-m-d"  # '1887-07-26'
# 短日期时间格式
SHORT_DATETIME_FORMAT = "Y-m-d H:i"  # '1887-07-26 18:59'
# 一周起始日：1 表示周一
FIRST_DAY_OF_WEEK = 1  # Monday (lundo)

# The *_INPUT_FORMATS strings use the Python strftime format syntax,
# see https://docs.python.org/library/datetime.html#strftime-strptime-behavior
# 表单日期输入接受的 strftime 格式列表
DATE_INPUT_FORMATS = [
    "%Y-%m-%d",  # '1887-07-26'
    "%y-%m-%d",  # '87-07-26'
    "%Y %m %d",  # '1887 07 26'
    "%Y.%m.%d",  # '1887.07.26'
    "%d-a de %b %Y",  # '26-a de jul 1887'
    "%d %b %Y",  # '26 jul 1887'
    "%d-a de %B %Y",  # '26-a de julio 1887'
    "%d %B %Y",  # '26 julio 1887'
    "%d %m %Y",  # '26 07 1887'
    "%d/%m/%Y",  # '26/07/1887'
]
# 表单时间输入格式
TIME_INPUT_FORMATS = [
    "%H:%M:%S",  # '18:59:00'
    "%H:%M",  # '18:59'
]
# 表单日期时间输入格式
DATETIME_INPUT_FORMATS = [
    "%Y-%m-%d %H:%M:%S",  # '1887-07-26 18:59:00'
    "%Y-%m-%d %H:%M",  # '1887-07-26 18:59'
    "%Y.%m.%d %H:%M:%S",  # '1887.07.26 18:59:00'
    "%Y.%m.%d %H:%M",  # '1887.07.26 18:59'
    "%d/%m/%Y %H:%M:%S",  # '26/07/1887 18:59:00'
    "%d/%m/%Y %H:%M",  # '26/07/1887 18:59'
    "%y-%m-%d %H:%M:%S",  # '87-07-26 18:59:00'
    "%y-%m-%d %H:%M",  # '87-07-26 18:59'
]
# 小数分隔符
DECIMAL_SEPARATOR = ","
# 千位分隔符（非断行空格）
THOUSAND_SEPARATOR = "\xa0"  # non-breaking space
# 数字分组位数（千分位）
NUMBER_GROUPING = 3
