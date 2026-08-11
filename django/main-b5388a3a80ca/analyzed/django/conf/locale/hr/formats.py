# This file is distributed under the same license as the Django package.
#
# The *_FORMAT strings use the Django date format syntax,
# see https://docs.djangoproject.com/en/dev/ref/templates/builtins/#date
# 克罗地亚语日期、时间与数字本地化格式
# 完整日期显示格式（日. 星期缩写 年.）
DATE_FORMAT = "j. E Y."
# 时间显示格式（24 小时制）
TIME_FORMAT = "H:i"
# 日期时间组合显示格式
DATETIME_FORMAT = "j. E Y. H:i"
# 仅年月时的显示格式
YEAR_MONTH_FORMAT = "F Y."
# 仅月日时的显示格式
MONTH_DAY_FORMAT = "j. F"
# 短日期格式（日.月.年.）
SHORT_DATE_FORMAT = "j.m.Y."
# 短日期时间格式
SHORT_DATETIME_FORMAT = "j.m.Y. H:i"
# 一周起始日：1 表示周一
FIRST_DAY_OF_WEEK = 1

# The *_INPUT_FORMATS strings use the Python strftime format syntax,
# see https://docs.python.org/library/datetime.html#strftime-strptime-behavior
# 保留 ISO 格式在列表首位
# 表单日期输入接受的 strftime 格式列表
DATE_INPUT_FORMATS = [
    "%Y-%m-%d",  # '2006-10-25'
    "%d.%m.%Y.",  # '25.10.2006.'
    "%d.%m.%y.",  # '25.10.06.'
    "%d. %m. %Y.",  # '25. 10. 2006.'
    "%d. %m. %y.",  # '25. 10. 06.'
]
# 表单日期时间输入格式
DATETIME_INPUT_FORMATS = [
    "%Y-%m-%d %H:%M:%S",  # '2006-10-25 14:30:59'
    "%Y-%m-%d %H:%M:%S.%f",  # '2006-10-25 14:30:59.000200'
    "%Y-%m-%d %H:%M",  # '2006-10-25 14:30'
    "%d.%m.%Y. %H:%M:%S",  # '25.10.2006. 14:30:59'
    "%d.%m.%Y. %H:%M:%S.%f",  # '25.10.2006. 14:30:59.000200'
    "%d.%m.%Y. %H:%M",  # '25.10.2006. 14:30'
    "%d.%m.%y. %H:%M:%S",  # '25.10.06. 14:30:59'
    "%d.%m.%y. %H:%M:%S.%f",  # '25.10.06. 14:30:59.000200'
    "%d.%m.%y. %H:%M",  # '25.10.06. 14:30'
    "%d. %m. %Y. %H:%M:%S",  # '25. 10. 2006. 14:30:59'
    "%d. %m. %Y. %H:%M:%S.%f",  # '25. 10. 2006. 14:30:59.000200'
    "%d. %m. %Y. %H:%M",  # '25. 10. 2006. 14:30'
    "%d. %m. %y. %H:%M:%S",  # '25. 10. 06. 14:30:59'
    "%d. %m. %y. %H:%M:%S.%f",  # '25. 10. 06. 14:30:59.000200'
    "%d. %m. %y. %H:%M",  # '25. 10. 06. 14:30'
]
# 小数分隔符
DECIMAL_SEPARATOR = ","
# 千位分隔符
THOUSAND_SEPARATOR = "."
# 数字分组位数（千分位）
NUMBER_GROUPING = 3
