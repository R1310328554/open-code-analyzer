# This file is distributed under the same license as the Django package.
#
# The *_FORMAT strings use the Django date format syntax,
# see https://docs.djangoproject.com/en/dev/ref/templates/builtins/#date
# 维吾尔语日期、时间与数字本地化格式
# 完整日期显示格式（日 月名 年）
DATE_FORMAT = "j F Y"
# 时间显示格式（24 小时制）
TIME_FORMAT = "G:i"
# 仅年月时的显示格式
YEAR_MONTH_FORMAT = "F Y"
# 仅月日时的显示格式
MONTH_DAY_FORMAT = "j F"
# 短日期格式（年/月/日）
SHORT_DATE_FORMAT = "Y/m/d"
# 短日期时间格式
SHORT_DATETIME_FORMAT = "Y/m/d G:i"
# 一周起始日：1 表示周一
FIRST_DAY_OF_WEEK = 1
# 小数分隔符
DECIMAL_SEPARATOR = "."
# 千位分隔符
THOUSAND_SEPARATOR = ","
# 数字分组位数（千分位）
NUMBER_GROUPING = 3
