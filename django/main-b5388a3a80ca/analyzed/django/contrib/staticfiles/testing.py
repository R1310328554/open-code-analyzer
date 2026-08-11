from django.contrib.staticfiles.handlers import StaticFilesHandler
from django.test import LiveServerTestCase


# 实时服务器测试基类 — 测试时通过 finder 透明提供静态资源，无需先 collectstatic
class StaticLiveServerTestCase(LiveServerTestCase):
    """
    Extend django.test.LiveServerTestCase to transparently overlay at test
    execution-time the assets provided by the staticfiles app finders. This
    means you don't need to run collectstatic before or as a part of your tests
    setup.
    """

    static_handler = StaticFilesHandler
