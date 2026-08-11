from . import Tags, register


# 对所有已注册模板引擎调用 engine.check()
@register(Tags.templates)
def check_templates(app_configs, **kwargs):
    """Check all registered template engines."""
    from django.template import engines

    errors = []
    for engine in engines.all():
        errors.extend(engine.check())
    return errors
