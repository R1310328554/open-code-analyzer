"""
django.contrib.admin.templatetags.base — 可覆盖 inclusion 模板的管理后台节点基类。

InclusionAdminNode 按 app/模型/global 优先级解析 admin 子目录下的模板。
"""
from django.template.exceptions import TemplateSyntaxError
from django.template.library import InclusionNode, parse_bits
from django.utils.inspect import getfullargspec


# 支持 per-model/per-app/global 三级模板覆盖的 inclusion 标签节点
class InclusionAdminNode(InclusionNode):
    """
    Template tag that allows its template to be overridden per model, per app,
    or globally.
    """

    def __init__(self, name, parser, token, func, template_name, takes_context=True):
        self.template_name = template_name
        params, varargs, varkw, defaults, kwonly, kwonly_defaults, _ = getfullargspec(
            func
        )
        if takes_context:
            if params and params[0] == "context":
                del params[0]
            else:
                function_name = func.__name__
                raise TemplateSyntaxError(
                    f"{name!r} sets takes_context=True so {function_name!r} "
                    "must have a first argument of 'context'"
                )
        bits = token.split_contents()
        args, kwargs = parse_bits(
            parser,
            bits[1:],
            params,
            varargs,
            varkw,
            defaults,
            kwonly,
            kwonly_defaults,
            bits[0],
        )
        super().__init__(func, takes_context, args, kwargs, filename=None)

    # 按 app_label、model_name 与全局路径依次查找 inclusion 模板
    def render(self, context):
        opts = context["opts"]
        app_label = opts.app_label.lower()
        object_name = opts.model_name
        # Load template for this render call. (Setting self.filename isn't
        # thread-safe.)
        context.render_context[self] = context.template.engine.select_template(
            [
                "admin/%s/%s/%s" % (app_label, object_name, self.template_name),
                "admin/%s/%s" % (app_label, self.template_name),
                "admin/%s" % self.template_name,
            ]
        )
        return super().render(context)
