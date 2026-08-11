from django.conf import settings
from django.contrib.messages import constants


# 合并 DEFAULT_TAGS 与 settings.MESSAGE_TAGS 返回级别标签映射
def get_level_tags():
    """
    Return the message level tags.
    """
    return {
        **constants.DEFAULT_TAGS,
        **getattr(settings, "MESSAGE_TAGS", {}),
    }
