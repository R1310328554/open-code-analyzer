from django.contrib.sites.models import Site
from django.db import models
from django.utils.translation import gettext_lazy as _


# URL 重定向记录：按站点将 old_path 映射到 new_path 或空（410 Gone）
class Redirect(models.Model):
    site = models.ForeignKey(Site, models.CASCADE, verbose_name=_("site"))
    old_path = models.CharField(
        _("redirect from"),
        max_length=200,
        db_index=True,
        help_text=_(
            "This should be an absolute path, excluding the domain name. Example: "
            "“/events/search/”."
        ),
    )
    new_path = models.CharField(
        _("redirect to"),
        max_length=200,
        blank=True,
        help_text=_(
            "This can be either an absolute path (as above) or a full URL "
            "starting with a scheme such as “https://”."
        ),
    )

    # 表名 django_redirect；站点与旧路径唯一；按 old_path 排序
    class Meta:
        verbose_name = _("redirect")
        verbose_name_plural = _("redirects")
        db_table = "django_redirect"
        unique_together = [["site", "old_path"]]
        ordering = ["old_path"]

    # 返回 “旧路径 ---> 新路径” 的可读字符串
    def __str__(self):
        return "%s ---> %s" % (self.old_path, self.new_path)
