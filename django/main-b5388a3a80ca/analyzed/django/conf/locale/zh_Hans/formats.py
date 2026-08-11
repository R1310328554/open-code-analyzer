# This file is distributed under the same license as the Django package.
#
# The *_FORMAT strings use the Django date format syntax,
# see https://docs.djangoproject.com/en/dev/ref/templates/builtins/#date
# 简体中文日期、时间与数字本地化格式
# 完整日期显示格式
DATE_FORMAT = "Y年n月j日"  # 2016年9月5日
# 时间显示格式（24 小时制）
TIME_FORMAT = "H:i"  # 20:45
# 日期时间组合显示格式
DATETIME_FORMAT = "Y年n月j日 H:i"  # 2016年9月5日 20:45
# 仅年月时的显示格式
YEAR_MONTH_FORMAT = "Y年n月"  # 2016年9月
# 仅月日时的显示格式
MONTH_DAY_FORMAT = "m月j日"  # 9月5日
# 短日期格式
SHORT_DATE_FORMAT = "Y年n月j日"  # 2016年9月5日
# 短日期时间格式
SHORT_DATETIME_FORMAT = "Y年n月j日 H:i"  # 2016年9月5日 20:45
# 一周起始日：1 表示周一
FIRST_DAY_OF_WEEK = 1  # 星期一 (Monday)

# The *_INPUT_FORMATS strings use the Python strftime format syntax,
# see https://docs.python.org/library/datetime.html#strftime-strptime-behavior
# 表单日期输入格式
DATE_INPUT_FORMATS = [
    "%Y/%m/%d",  # '2016/09/05'
    "%Y-%m-%d",  # '2016-09-05'
    "%Y年%n月%j日",  # '2016年9月5日'
]

# 表单时间输入格式
TIME_INPUT_FORMATS = [
    "%H:%M",  # '20:45'
    "%H:%M:%S",  # '20:45:29'
    "%H:%M:%S.%f",  # '20:45:29.000200'
]

# 表单日期时间输入格式
DATETIME_INPUT_FORMATS = [
    "%Y/%m/%d %H:%M",  # '2016/09/05 20:45'
    "%Y-%m-%d %H:%M",  # '2016-09-05 20:45'
    "%Y年%n月%j日 %H:%M",  # '2016年9月5日 14:45'
    "%Y/%m/%d %H:%M:%S",  # '2016/09/05 20:45:29'
    "%Y-%m-%d %H:%M:%S",  # '2016-09-05 20:45:29'
    "%Y年%n月%j日 %H:%M:%S",  # '2016年9月5日 20:45:29'
    "%Y/%m/%d %H:%M:%S.%f",  # '2016/09/05 20:45:29.000200'
    "%Y-%m-%d %H:%M:%S.%f",  # '2016-09-05 20:45:29.000200'
    "%Y年%n月%j日 %H:%n:%S.%f",  # '2016年9月5日 20:45:29.000200'
]

# 小数分隔符
DECIMAL_SEPARATOR = "."
# 千位分隔符（空字符串表示不分组）
THOUSAND_SEPARATOR = ""
# 数字分组位数（每四位一组）
NUMBER_GROUPING = 4
