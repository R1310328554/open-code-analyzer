from importlib import import_module

from django.conf import settings
from django.core.management.base import BaseCommand, CommandError


# 清理过期会话 — 调用当前 SESSION_ENGINE 的 clear_expired
class Command(BaseCommand):
    help = (
        "Can be run as a cronjob or directly to clean out expired sessions "
        "when the backend supports it."
    )

    # 动态加载引擎并清理；不支持时抛出 CommandError
    def handle(self, **options):
        engine = import_module(settings.SESSION_ENGINE)
        try:
            engine.SessionStore.clear_expired()
        except NotImplementedError:
            raise CommandError(
                "Session engine '%s' doesn't support clearing expired "
                "sessions." % settings.SESSION_ENGINE
            )
