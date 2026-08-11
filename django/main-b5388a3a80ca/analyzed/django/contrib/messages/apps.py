from django.apps import AppConfig
from django.contrib.messages.storage import base
from django.contrib.messages.utils import get_level_tags
from django.core.signals import setting_changed
from django.utils.functional import SimpleLazyObject
from django.utils.translation import gettext_lazy as _


# MESSAGE_TAGS 变更时刷新 LEVEL_TAGS 懒对象
def update_level_tags(setting, **kwargs):
    if setting == "MESSAGE_TAGS":
        base.LEVEL_TAGS = SimpleLazyObject(get_level_tags)


# messages 应用配置 — 注册设置变更信号
class MessagesConfig(AppConfig):
    name = "django.contrib.messages"
    verbose_name = _("Messages")

    # 应用就绪时连接 setting_changed 以更新级别标签
    def ready(self):
        setting_changed.connect(update_level_tags)
