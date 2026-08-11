"""
django.utils.translation.reloader — .mo 文件变更监控与翻译缓存失效。
"""

from pathlib import Pathfrom pathlib import Path

from asgiref.local import Local

from django.apps import apps
from django.utils.autoreload import is_django_module


# autoreload：注册 locale 目录下 .mo 文件监控
def watch_for_translation_changes(sender, **kwargs):
    """Register file watchers for .mo files in potential locale paths."""
    from django.conf import settings

    if settings.USE_I18N:
        directories = [Path("locale")]
        directories.extend(
            Path(config.path) / "locale"
            for config in apps.get_app_configs()
            if not is_django_module(config.module)
        )
        directories.extend(Path(p) for p in settings.LOCALE_PATHS)
        for path in directories:
            sender.watch_dir(path, "**/*.mo")


# .mo 变更时清空 gettext 与 trans_real 缓存
def translation_file_changed(sender, file_path, **kwargs):
    """Clear the internal translations cache if a .mo file is modified."""
    if file_path.suffix == ".mo":
        import gettext

        from django.utils.translation import trans_real

        gettext._translations = {}
        trans_real._translations = {}
        trans_real._default = None
        trans_real._active = Local()
        return True
