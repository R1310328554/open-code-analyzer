"""django.contrib.admin.models — 后台操作审计日志模型。

LogEntry 记录管理员对模型的增删改操作，供历史页与 django_admin_log 表使用。
"""
import json

from django.conf import settings
from django.contrib.admin.utils import quote
from django.contrib.contenttypes.models import ContentType
from django.db import models
from django.urls import NoReverseMatch, reverse
from django.utils import timezone
from django.utils.text import get_text_list
from django.utils.translation import gettext
from django.utils.translation import gettext_lazy as _

# 操作类型常量：新增
ADDITION = 1
# 操作类型常量：修改
CHANGE = 2
# 操作类型常量：删除
DELETION = 3

ACTION_FLAG_CHOICES = [
    (ADDITION, _("Addition")),
    (CHANGE, _("Change")),
    (DELETION, _("Deletion")),
]


# 日志条目管理器：批量写入后台操作记录
class LogEntryManager(models.Manager):
    use_in_migrations = True

    # 为 queryset 中每个对象写入一条或多条 LogEntry
    def log_actions(
        self, user_id, queryset, action_flag, change_message="", *, single_object=False
    ):
        if isinstance(change_message, list):
            change_message = json.dumps(change_message)

        log_entry_list = [
            self.model(
                user_id=user_id,
                content_type_id=ContentType.objects.get_for_model(
                    obj, for_concrete_model=False
                ).id,
                object_id=obj.pk,
                object_repr=str(obj)[:200],
                action_flag=action_flag,
                change_message=change_message,
            )
            for obj in queryset
        ]

        if len(log_entry_list) == 1:
            instance = log_entry_list[0]
            instance.save()
            if single_object:
                return instance
            return [instance]

        return self.model.objects.bulk_create(log_entry_list)


# 单条后台操作日志，关联用户、内容类型与目标对象
class LogEntry(models.Model):
    action_time = models.DateTimeField(
        _("action time"),
        default=timezone.now,
        editable=False,
    )
    user = models.ForeignKey(
        settings.AUTH_USER_MODEL,
        models.CASCADE,
        verbose_name=_("user"),
    )
    content_type = models.ForeignKey(
        ContentType,
        models.SET_NULL,
        verbose_name=_("content type"),
        blank=True,
        null=True,
    )
    object_id = models.TextField(_("object id"), blank=True, null=True)
    # Translators: 'repr' means representation
    # (https://docs.python.org/library/functions.html#repr)
    object_repr = models.CharField(_("object repr"), max_length=200)
    action_flag = models.PositiveSmallIntegerField(
        _("action flag"), choices=ACTION_FLAG_CHOICES
    )
    # change_message is either a string or a JSON structure
    change_message = models.TextField(_("change message"), blank=True)

    objects = LogEntryManager()

    class Meta:
        verbose_name = _("log entry")
        verbose_name_plural = _("log entries")
        db_table = "django_admin_log"
        ordering = ["-action_time"]

    def __repr__(self):
        return str(self.action_time)

    def __str__(self):
        if self.is_addition():
            return gettext("Added “%(object)s”.") % {"object": self.object_repr}
        elif self.is_change():
            return gettext("Changed “%(object)s” — %(changes)s") % {
                "object": self.object_repr,
                "changes": self.get_change_message(),
            }
        elif self.is_deletion():
            return gettext("Deleted “%(object)s.”") % {"object": self.object_repr}

        return gettext("LogEntry Object")

    # 判断是否为「新增」操作
    def is_addition(self):
        return self.action_flag == ADDITION

    # 判断是否为「修改」操作
    def is_change(self):
        return self.action_flag == CHANGE

    # 判断是否为「删除」操作
    def is_deletion(self):
        return self.action_flag == DELETION

    # 解析 change_message（支持 JSON 结构）并返回可读的变更描述
    def get_change_message(self):
        """
        If self.change_message is a JSON structure, interpret it as a change
        string, properly translated.
        """
        if self.change_message and self.change_message[0] == "[":
            try:
                change_message = json.loads(self.change_message)
            except json.JSONDecodeError:
                return self.change_message
            messages = []
            for sub_message in change_message:
                if "added" in sub_message:
                    if sub_message["added"]:
                        sub_message["added"]["name"] = gettext(
                            sub_message["added"]["name"]
                        )
                        messages.append(
                            gettext("Added {name} “{object}”.").format(
                                **sub_message["added"]
                            )
                        )
                    else:
                        messages.append(gettext("Added."))

                elif "changed" in sub_message:
                    sub_message["changed"]["fields"] = get_text_list(
                        [
                            gettext(field_name)
                            for field_name in sub_message["changed"]["fields"]
                        ],
                        gettext("and"),
                    )
                    if "name" in sub_message["changed"]:
                        sub_message["changed"]["name"] = gettext(
                            sub_message["changed"]["name"]
                        )
                        messages.append(
                            gettext("Changed {fields} for {name} “{object}”.").format(
                                **sub_message["changed"]
                            )
                        )
                    else:
                        messages.append(
                            gettext("Changed {fields}.").format(
                                **sub_message["changed"]
                            )
                        )

                elif "deleted" in sub_message:
                    sub_message["deleted"]["name"] = gettext(
                        sub_message["deleted"]["name"]
                    )
                    messages.append(
                        gettext("Deleted {name} “{object}”.").format(
                            **sub_message["deleted"]
                        )
                    )

            change_message = " ".join(msg[0].upper() + msg[1:] for msg in messages)
            return change_message or gettext("No fields changed.")
        else:
            return self.change_message

    # 根据 content_type 与 object_id 取回被编辑的模型实例
    def get_edited_object(self):
        """Return the edited object represented by this log entry."""
        return self.content_type.get_object_for_this_type(pk=self.object_id)

    # 返回指向该对象后台编辑页的 URL，无法解析时返回 None
    def get_admin_url(self):
        """
        Return the admin URL to edit the object represented by this log entry.
        """
        if self.content_type and self.object_id:
            url_name = "admin:%s_%s_change" % (
                self.content_type.app_label,
                self.content_type.model,
            )
            try:
                return reverse(url_name, args=(quote(self.object_id),))
            except NoReverseMatch:
                pass
        return None
