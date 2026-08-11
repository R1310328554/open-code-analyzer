"""
This module allows importing AbstractBaseSession even
when django.contrib.sessions is not in INSTALLED_APPS.
"""

# 会话抽象基类模块 — 未安装 sessions 应用时也可导入 AbstractBaseSessionwhen django.contrib.sessions is not in INSTALLED_APPS.
"""

from django.db import models
from django.utils.translation import gettext_lazy as _


# 会话模型管理器 — 负责编码/保存/删除数据库中的会话记录
class BaseSessionManager(models.Manager):
    # 通过 SessionStore 将会话字典序列化为字符串
    def encode(self, session_dict):
        """
        Return the given session dictionary serialized and encoded as a string.
        """
        session_store_class = self.model.get_session_store_class()
        return session_store_class().encode(session_dict)

    # 编码后写入或删除空会话记录
    def save(self, session_key, session_dict, expire_date):
        s = self.model(session_key, self.encode(session_dict), expire_date)
        if session_dict:
            s.save()
        else:
            s.delete()  # Clear sessions with no data.
        return s


# 会话抽象模型 — 定义 session_key、session_data 与 expire_date 字段
class AbstractBaseSession(models.Model):
    session_key = models.CharField(_("session key"), max_length=40, primary_key=True)
    session_data = models.TextField(_("session data"))
    expire_date = models.DateTimeField(_("expire date"), db_index=True)

    objects = BaseSessionManager()

    class Meta:
        abstract = True
        verbose_name = _("session")
        verbose_name_plural = _("sessions")

    # 返回 session_key 字符串
    def __str__(self):
        return self.session_key

    # 子类需实现：返回对应的 SessionStore 类
    @classmethod
    def get_session_store_class(cls):
        raise NotImplementedError

    # 解码 session_data 为 Python 字典
    def get_decoded(self):
        session_store_class = self.get_session_store_class()
        return session_store_class().decode(self.session_data)
