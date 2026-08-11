# This file is distributed under the same license as the Django package.
#
# The *_FORMAT strings use the Django date format syntax,
# see https://docs.djangoproject.com/en/dev/ref/templates/builtins/#date
# 日语日期、时间与数字本地化格式
# 完整日期显示格式（年 月 日，使用汉字年月日）
DATE_FORMAT = "Y年n月j日"
# 时间显示格式（24 小时制）
TIME_FORMAT = "G:i"
# 日期时间组合显示格式
DATETIME_FORMAT = "Y年n月j日G:i"
# 仅年月时的显示格式
YEAR_MONTH_FORMAT = "Y年n月"
# 仅月日时的显示格式
MONTH_DAY_FORMAT = "n月j日"
# 短日期格式（年/月/日）
SHORT_DATE_FORMAT = "Y/m/d"
# 短日期时间格式
SHORT_DATETIME_FORMAT = "Y/m/d G:i"
# 一周起始日（未覆盖）
# FIRST_DAY_OF_WEEK =

# The *_INPUT_FORMATS strings use the Python strftime format syntax,
# see https://docs.python.org/library/datetime.html#strftime-strptime-behavior
# 表单日期输入格式（未覆盖）
# DATE_INPUT_FORMATS =
# 表单时间输入格式（未覆盖）
# TIME_INPUT_FORMATS =
# 表单日期时间输入格式（未覆盖）
# DATETIME_INPUT_FORMATS =
# 小数分隔符
DECIMAL_SEPARATOR = "."
# 千位分隔符
THOUSAND_SEPARATOR = ","
# 数字分组位数（千分位）
NUMBER_GROUPING = 3
