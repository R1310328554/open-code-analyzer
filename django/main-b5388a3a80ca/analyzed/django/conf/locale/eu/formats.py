# This file is distributed under the same license as the Django package.
#
# The *_FORMAT strings use the Django date format syntax,
# see https://docs.djangoproject.com/en/dev/ref/templates/builtins/#date
# 巴斯克语日期、时间与数字本地化格式
# 完整日期显示格式（年(e)ko 月 日）
DATE_FORMAT = r"Y(\e)\k\o N\k j"
# 时间显示格式（24 小时制）
TIME_FORMAT = "H:i"
# 日期时间组合显示格式
DATETIME_FORMAT = r"Y(\e)\k\o N\k j, H:i"
# 仅年月时的显示格式
YEAR_MONTH_FORMAT = r"Y(\e)\k\o F"
# 仅月日时的显示格式（ren 日）
MONTH_DAY_FORMAT = r"F\r\e\n j\a"
# 短日期格式（ISO 年-月-日）
SHORT_DATE_FORMAT = "Y-m-d"
# 短日期时间格式
SHORT_DATETIME_FORMAT = "Y-m-d H:i"
# 一周起始日：1 表示周一
FIRST_DAY_OF_WEEK = 1  # Astelehena

# The *_INPUT_FORMATS strings use the Python strftime format syntax,
# see https://docs.python.org/library/datetime.html#strftime-strptime-behavior
# DATE_INPUT_FORMATS =
# TIME_INPUT_FORMATS =
# DATETIME_INPUT_FORMATS =
# 小数分隔符
DECIMAL_SEPARATOR = ","
# 千位分隔符
THOUSAND_SEPARATOR = "."
# 数字分组位数（千分位）
NUMBER_GROUPING = 3
