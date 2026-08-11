"""
django.contrib.flatpages.forms — FlatPage 后台表单。

校验 URL 格式、首尾斜杠规则及同站点 URL 唯一性。
"""
from django import forms
from django.conf import settings
from django.contrib.flatpages.models import FlatPage
from django.core.exceptions import ValidationError
from django.utils.translation import gettext
from django.utils.translation import gettext_lazy as _


# ModelForm：RegexField 约束 url，clean 检查重复
class FlatpageForm(forms.ModelForm):
    url = forms.RegexField(
        label=_("URL"),
        max_length=100,
        regex=r"^[-\w/.~]+$",
        help_text=_(
            "Example: “/about/contact/”. Make sure to have leading and trailing "
            "slashes."
        ),
        error_messages={
            "invalid": _(
                "This value must contain only letters, numbers, dots, "
                "underscores, dashes, slashes or tildes."
            ),
        },
    )

    class Meta:
        model = FlatPage
        fields = "__all__"

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        if not self._trailing_slash_required():
            self.fields["url"].help_text = _(
                "Example: “/about/contact”. Make sure to have a leading slash."
            )

    # APPEND_SLASH 且启用 CommonMiddleware 时要求尾部斜杠
    def _trailing_slash_required(self):
        return (
            settings.APPEND_SLASH
            and "django.middleware.common.CommonMiddleware" in settings.MIDDLEWARE
        )

    # 校验 leading/trailing slash 规则
    def clean_url(self):
        url = self.cleaned_data["url"]
        if not url.startswith("/"):
            raise ValidationError(
                gettext("URL is missing a leading slash."),
                code="missing_leading_slash",
            )
        if self._trailing_slash_required() and not url.endswith("/"):
            raise ValidationError(
                gettext("URL is missing a trailing slash."),
                code="missing_trailing_slash",
            )
        return url

    # 同站点下 url 不得与已有 FlatPage 重复
    def clean(self):
        url = self.cleaned_data.get("url")
        sites = self.cleaned_data.get("sites")

        same_url = FlatPage.objects.filter(url=url)
        if self.instance.pk:
            same_url = same_url.exclude(pk=self.instance.pk)

        if sites and same_url.filter(sites__in=sites).exists():
            for site in sites:
                if same_url.filter(sites=site).exists():
                    raise ValidationError(
                        _("Flatpage with url %(url)s already exists for site %(site)s"),
                        code="duplicate_url",
                        params={"url": url, "site": site},
                    )

        return super().clean()
