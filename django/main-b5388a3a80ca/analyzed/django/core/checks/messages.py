# 系统检查消息严重级别常量
# Levels
DEBUG = 10
INFO = 20
WARNING = 30
ERROR = 40
CRITICAL = 50


# 系统检查消息基类 — 封装级别、正文、提示、关联对象与错误 ID
class CheckMessage:
    # 构造检查消息并校验 level 为整数
    def __init__(self, level, msg, hint=None, obj=None, id=None):
        if not isinstance(level, int):
            raise TypeError("The first argument should be level.")
        self.level = level
        self.msg = msg
        self.hint = hint
        self.obj = obj
        self.id = id

    # 按各属性值比较两条消息是否相等
    def __eq__(self, other):
        return isinstance(other, self.__class__) and all(
            getattr(self, attr) == getattr(other, attr)
            for attr in ["level", "msg", "hint", "obj", "id"]
        )

    # 格式化输出 obj、id、msg 与 hint
    def __str__(self):
        from django.db import models

        if self.obj is None:
            obj = "?"
        elif isinstance(self.obj, models.base.ModelBase):
            # We need to hardcode ModelBase and Field cases because its __str__
            # method doesn't return "applabel.modellabel" and cannot be
            # changed.
            obj = self.obj._meta.label
        else:
            obj = str(self.obj)
        id = "(%s) " % self.id if self.id else ""
        hint = "\n\tHINT: %s" % self.hint if self.hint else ""
        return "%s: %s%s%s" % (obj, id, self.msg, hint)

    # 调试用的结构化 repr
    def __repr__(self):
        return "<%s: level=%r, msg=%r, hint=%r, obj=%r, id=%r>" % (
            self.__class__.__name__,
            self.level,
            self.msg,
            self.hint,
            self.obj,
            self.id,
        )

    # 判断消息级别是否达到指定阈值（默认 ERROR）
    def is_serious(self, level=ERROR):
        return self.level >= level

    # 检查 id 是否在 SILENCED_SYSTEM_CHECKS 中被静默
    def is_silenced(self):
        from django.conf import settings

        return self.id in settings.SILENCED_SYSTEM_CHECKS


# DEBUG 级别检查消息
class Debug(CheckMessage):
    # 固定 level 为 DEBUG 后委托基类
    def __init__(self, *args, **kwargs):
        super().__init__(DEBUG, *args, **kwargs)


# INFO 级别检查消息
class Info(CheckMessage):
    def __init__(self, *args, **kwargs):
        super().__init__(INFO, *args, **kwargs)


# WARNING 级别检查消息
class Warning(CheckMessage):
    def __init__(self, *args, **kwargs):
        super().__init__(WARNING, *args, **kwargs)


# ERROR 级别检查消息
class Error(CheckMessage):
    def __init__(self, *args, **kwargs):
        super().__init__(ERROR, *args, **kwargs)


# CRITICAL 级别检查消息
class Critical(CheckMessage):
    def __init__(self, *args, **kwargs):
        super().__init__(CRITICAL, *args, **kwargs)
