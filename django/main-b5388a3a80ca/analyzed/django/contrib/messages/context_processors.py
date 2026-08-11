from django.contrib.messages.api import get_messages
from django.contrib.messages.constants import DEFAULT_LEVELS


# 模板上下文处理器 — 提供 messages 与 DEFAULT_MESSAGE_LEVELS
def messages(request):
    """
    Return a lazy 'messages' context variable as well as
    'DEFAULT_MESSAGE_LEVELS'.
    """
    return {
        "messages": get_messages(request),
        "DEFAULT_MESSAGE_LEVELS": DEFAULT_LEVELS,
    }
