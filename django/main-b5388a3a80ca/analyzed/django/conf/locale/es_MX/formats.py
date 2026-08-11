# This file is distributed under the same license as the Django package.
#
# 墨西哥西班牙语日期、时间与数字本地化格式
# 完整日期显示格式（日 de 月 de 年）
DATE_FORMAT = r"j \d\e F \d\e Y"
# 时间显示格式（24 小时制）
TIME_FORMAT = "H:i"
# 日期时间组合显示格式
DATETIME_FORMAT = r"j \d\e F \d\e Y \a \l\a\s H:i"
# 仅年月时的显示格式
YEAR_MONTH_FORMAT = r"F \d\e Y"
# 仅月日时的显示格式
MONTH_DAY_FORMAT = r"j \d\e F"
# 短日期格式（日/月/年）
SHORT_DATE_FORMAT = "d/m/Y"
# 短日期时间格式
SHORT_DATETIME_FORMAT = "d/m/Y H:i"
# 一周起始日：1 表示周一（ISO 8601）
FIRST_DAY_OF_WEEK = 1  # Monday: ISO 8601
# 表单日期输入接受的 strftime 格式列表
DATE_INPUT_FORMATS = [
    "%d/%m/%Y",  # '25/10/2006'
    "%d/%m/%y",  # '25/10/06'
    "%Y%m%d",  # '20061025'
]
# 表单日期时间输入格式
DATETIME_INPUT_FORMATS = [
    "%d/%m/%Y %H:%M:%S",
    "%d/%m/%Y %H:%M:%S.%f",
    "%d/%m/%Y %H:%M",
    "%d/%m/%y %H:%M:%S",
    "%d/%m/%y %H:%M:%S.%f",
    "%d/%m/%y %H:%M",
]
# 小数分隔符（逗号亦符合 NOM-008-SCFI-2002，但较少见）
DECIMAL_SEPARATOR = "."  # ',' is also official (less common): NOM-008-SCFI-2002
# 千位分隔符
THOUSAND_SEPARATOR = ","
# 数字分组位数（千分位）
NUMBER_GROUPING = 3
