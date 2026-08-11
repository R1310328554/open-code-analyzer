# This file is distributed under the same license as the Django package.
#
# The *_FORMAT strings use the Django date format syntax,
# see https://docs.djangoproject.com/en/dev/ref/templates/builtins/#date
# 波斯语（伊朗）日期、时间与数字本地化格式
# 完整日期显示格式（日 月 年）
DATE_FORMAT = "j F Y"
# 时间显示格式（24 小时制，无前置零）
TIME_FORMAT = "G:i"
# 日期时间组合显示格式（含波斯语「ساعت」表示时间）
DATETIME_FORMAT = "j F Y، ساعت G:i"
# 仅年月时的显示格式
YEAR_MONTH_FORMAT = "F Y"
# 仅月日时的显示格式
MONTH_DAY_FORMAT = "j F"
# 短日期格式（年/月/日，斜杠分隔）
SHORT_DATE_FORMAT = "Y/n/j"
# 短日期时间格式
SHORT_DATETIME_FORMAT = "Y/n/j،‏ G:i"
# 一周起始日：6 表示周六（伊朗历惯例）
FIRST_DAY_OF_WEEK = 6

# The *_INPUT_FORMATS strings use the Python strftime format syntax,
# see https://docs.python.org/library/datetime.html#strftime-strptime-behavior
# DATE_INPUT_FORMATS =
# TIME_INPUT_FORMATS =
# DATETIME_INPUT_FORMATS =
# 小数分隔符
DECIMAL_SEPARATOR = "."
# 千位分隔符
THOUSAND_SEPARATOR = ","
# NUMBER_GROUPING =
