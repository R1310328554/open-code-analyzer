# This file is distributed under the same license as the Django package.
#
# The *_FORMAT strings use the Django date format syntax,
# see https://docs.djangoproject.com/en/dev/ref/templates/builtins/#date
# 希伯来语日期、时间与数字本地化格式
# 完整日期显示格式（日 ב月名 年，ב 为希伯来语“在”）
DATE_FORMAT = "j בF Y"
# 时间显示格式（24 小时制）
TIME_FORMAT = "H:i"
# 日期时间组合显示格式
DATETIME_FORMAT = "j בF Y H:i"
# 仅年月时的显示格式
YEAR_MONTH_FORMAT = "F Y"
# 仅月日时的显示格式
MONTH_DAY_FORMAT = "j בF"
# 短日期格式（日/月/年）
SHORT_DATE_FORMAT = "d/m/Y"
# 短日期时间格式
SHORT_DATETIME_FORMAT = "d/m/Y H:i"
# 一周起始日（未覆盖，希伯来语地区通常为周日）
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
# 数字分组位数（未覆盖）
# NUMBER_GROUPING =
