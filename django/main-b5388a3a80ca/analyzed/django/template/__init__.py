"""
django.template — 模板子系统公共入口。

Django's support for templates."""
Django's support for templates.

The django.template namespace contains two independent subsystems:

1. Multiple Template Engines: support for pluggable template backends,
   built-in backends and backend-independent APIs
2. Django Template Language: Django's own template engine, including its
   built-in loaders, context processors, tags and filters.

Ideally these subsystems would be implemented in distinct packages. However
keeping them together made the implementation of Multiple Template Engines
less disruptive .

Here's a breakdown of which modules belong to which subsystem.

Multiple Template Engines:

- django.template.backends.*
- django.template.loader
- django.template.response

Django Template Language:

- django.template.base
- django.template.context
- django.template.context_processors
- django.template.loaders.*
- django.template.debug
- django.template.defaultfilters
- django.template.defaulttags
- django.template.engine
- django.template.loader_tags
- django.template.smartif

Shared:

- django.template.utils

"""

# 多模板引擎：Engine 与 engines 全局处理器

from .engine import Engine# Multiple Template Engines

from .engine import Engine
from .utils import EngineHandler

engines = EngineHandler()

__all__ = ("Engine", "engines")


# Django 模板语言（DTL）：Context、Template、异常与 Node 等

# Public exceptions# Django Template Language

# Public exceptions
from .base import VariableDoesNotExist  # NOQA isort:skip
from .context import Context, ContextPopException, RequestContext  # NOQA isort:skip
from .exceptions import TemplateDoesNotExist, TemplateSyntaxError  # NOQA isort:skip

# Template parts
from .base import (  # NOQA isort:skip
    Node,
    NodeList,
    Origin,
    PartialTemplate,
    Template,
    Variable,
)

# Library management
from .library import Library  # NOQA isort:skip

# 导入 autoreload 以注册开发服务器模板热重载信号
# Import the .autoreload module to trigger the registrations of signals.
from . import autoreload  # NOQA isort:skip

__all__ += ("Template", "Context", "RequestContext")
