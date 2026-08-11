# These are versions of the functions in django.utils.translation.trans_real
# that don't actually do anything. This is purely for performance, so that
# settings.USE_I18N = False can use this module rather than trans_real.py.

# django.utils.translation.trans_null — USE_I18N=False 时的空操作翻译后端。
# settings.USE_I18N = False can use this module rather than trans_real.py.

from django.conf import settings


# 恒等返回原文
def gettext(message):
    return message


gettext_noop = gettext_lazy = _ = gettext


# 按 number 选择单复数形式，不翻译
def ngettext(singular, plural, number):
    if number == 1:
        return singular
    return plural


ngettext_lazy = ngettext


def pgettext(context, message):
    return gettext(message)


def npgettext(context, singular, plural, number):
    return ngettext(singular, plural, number)


# 空操作
def activate(x):
    return None


def deactivate():
    return None


deactivate_all = deactivate


# 返回 settings.LANGUAGE_CODE
def get_language():
    return settings.LANGUAGE_CODE


def get_language_bidi():
    return settings.LANGUAGE_CODE in settings.LANGUAGES_BIDI


def check_for_language(x):
    return True


def get_language_from_request(request, check_path=False):
    return settings.LANGUAGE_CODE


def get_language_from_path(request):
    return None


# 仅匹配默认 LANGUAGE_CODE
def get_supported_language_variant(lang_code, strict=False):
    if lang_code and lang_code.lower() == settings.LANGUAGE_CODE.lower():
        return lang_code
    else:
        raise LookupError(lang_code)
