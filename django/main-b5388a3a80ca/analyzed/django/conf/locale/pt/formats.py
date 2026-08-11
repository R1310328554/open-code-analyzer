# This file is distributed under the same license as the Django package.
#
# The *_FORMAT strings use the Django date format syntax,
# see https://docs.djangoproject.com/en/dev/ref/templates/builtins/#date
# 葡萄牙语日期、时间与数字本地化格式
# 完整日期显示格式（日 de 月名 de 年）
DATE_FORMAT = r"j \d\e F \d\e Y"
# 时间显示格式（24 小时制）
TIME_FORMAT = "H:i"
# 日期时间组合显示格式
DATETIME_FORMAT = r"j \d\e F \d\e Y à\s H:i"
# 仅年月时的显示格式
YEAR_MONTH_FORMAT = r"F \d\e Y"
# 仅月日时的显示格式
MONTH_DAY_FORMAT = r"j \d\e F"
# 短日期格式（日/月/年）
SHORT_DATE_FORMAT = "d/m/Y"
# 短日期时间格式
SHORT_DATETIME_FORMAT = "d/m/Y H:i"
# 一周起始日：0 表示周日
FIRST_DAY_OF_WEEK = 0  # Sunday

# The *_INPUT_FORMATS strings use the Python strftime format syntax,
# see https://docs.python.org/library/datetime.html#strftime-strptime-behavior
# Kept ISO formats as they are in first position
# 表单日期输入格式；ISO 格式优先
DATE_INPUT_FORMATS = [
    "%Y-%m-%d",  # '2006-10-25'
    "%d/%m/%Y",  # '25/10/2006'
    "%d/%m/%y",  # '25/10/06'
    # "%d de %b de %Y",  # '25 de Out de 2006'
    # "%d de %b, %Y",  # '25 Out, 2006'
    # "%d de %B de %Y",  # '25 de Outubro de 2006'
    # "%d de %B, %Y",  # '25 de Outubro, 2006'
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
DECIMAL_SEPARATOR = ","
# 千位分隔符
THOUSAND_SEPARATOR = "."
# 数字分组位数（千分位）
NUMBER_GROUPING = 3
