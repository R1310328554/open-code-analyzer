"""
django.utils.duration — timedelta 格式化与 ISO 8601 字符串。

供 admin 与序列化使用的非英语特定 duration 表示。
"""

import datetimeimport datetime


# 分解 timedelta 为 days/hours/minutes/seconds/microseconds
def _get_duration_components(duration):
    days = duration.days
    seconds = duration.seconds
    microseconds = duration.microseconds

    minutes = seconds // 60
    seconds %= 60

    hours = minutes // 60
    minutes %= 60

    return days, hours, minutes, seconds, microseconds


# 人类可读 duration 字符串（非 locale 绑定）
def duration_string(duration):
    """Version of str(timedelta) which is not English specific."""
    days, hours, minutes, seconds, microseconds = _get_duration_components(duration)

    string = "{:02d}:{:02d}:{:02d}".format(hours, minutes, seconds)
    if days:
        string = "{} ".format(days) + string
    if microseconds:
        string += ".{:06d}".format(microseconds)

    return string


# ISO 8601 duration 如 P1DT02:03:04.000005S
def duration_iso_string(duration):
    if duration < datetime.timedelta(0):
        sign = "-"
        duration *= -1
    else:
        sign = ""

    days, hours, minutes, seconds, microseconds = _get_duration_components(duration)
    ms = ".{:06d}".format(microseconds) if microseconds else ""
    return "{}P{}DT{:02d}H{:02d}M{:02d}{}S".format(
        sign, days, hours, minutes, seconds, ms
    )


# timedelta 总微秒数
def duration_microseconds(delta):
    return (24 * 60 * 60 * delta.days + delta.seconds) * 1000000 + delta.microseconds
