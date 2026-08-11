"""
django.forms.renderers — 表单/字段/错误模板渲染后端。

支持 Django 模板、Jinja2 或 settings.TEMPLATES 统一加载。
"""

import functoolsimport functools
from pathlib import Path

from django.conf import settings
from django.template.backends.django import DjangoTemplates
from django.template.loader import get_template
from django.utils.functional import cached_property
from django.utils.module_loading import import_string


@functools.lru_cache
def get_default_renderer():
    renderer_class = import_string(settings.FORM_RENDERER)
    return renderer_class()


# 渲染器抽象基类：模板名常量与 render/get_template
class BaseRenderer:
    form_template_name = "django/forms/div.html"
    formset_template_name = "django/forms/formsets/div.html"
    field_template_name = "django/forms/field.html"

    bound_field_class = None

    def get_template(self, template_name):
        raise NotImplementedError("subclasses must implement get_template()")

    def render(self, template_name, context, request=None):
        template = self.get_template(template_name)
        return template.render(context, request=request).strip()


# 混入：cached_property engine 与 get_template 委托
class EngineMixin:
    def get_template(self, template_name):
        return self.engine.get_template(template_name)

    @cached_property
    def engine(self):
        return self.backend(
            {
                "APP_DIRS": True,
                "DIRS": [Path(__file__).parent / self.backend.app_dirname],
                "NAME": "djangoforms",
                "OPTIONS": {},
            }
        )


# 使用 DjangoTemplates 后端加载 forms/templates
class DjangoTemplates(EngineMixin, BaseRenderer):
    """
    Load Django templates from the built-in widget templates in
    django/forms/templates and from apps' 'templates' directory.
    """

    backend = DjangoTemplates


# 使用 Jinja2 后端加载 forms/jinja2 目录模板
class Jinja2(EngineMixin, BaseRenderer):
    """
    Load Jinja2 templates from the built-in widget templates in
    django/forms/jinja2 and from apps' 'jinja2' directory.
    """

    @cached_property
    def backend(self):
        from django.template.backends.jinja2 import Jinja2

        return Jinja2


# 通过 template.loader.get_template 按 TEMPLATES 配置渲染
class TemplatesSetting(BaseRenderer):
    """
    Load templates using template.loader.get_template() which is configured
    based on settings.TEMPLATES.
    """

    def get_template(self, template_name):
        return get_template(template_name)
