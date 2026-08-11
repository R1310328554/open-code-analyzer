"""
django.contrib.contenttypes.checks — ContentType 系统检查。

启动时扫描 GenericForeignKey 字段配置与 model_name 长度限制。
"""
from itertools import chain

from django.apps import apps
from django.core.checks import Error


# 遍历 GenericForeignKeyDescriptor 并调用 field.check()
def check_generic_foreign_keys(app_configs, **kwargs):
    from .fields import GenericForeignKeyDescriptor

    if app_configs is None:
        models = apps.get_models()
    else:
        models = chain.from_iterable(
            app_config.get_models() for app_config in app_configs
        )
    errors = []
    descriptors = (
        obj
        for model in models
        for obj in vars(model).values()
        if isinstance(obj, GenericForeignKeyDescriptor)
    )
    for descriptor in descriptors:
        errors.extend(descriptor.field.check())
    return errors


# model_name 不得超过 100 字符（contenttypes.E005）
def check_model_name_lengths(app_configs, **kwargs):
    if app_configs is None:
        models = apps.get_models()
    else:
        models = chain.from_iterable(
            app_config.get_models() for app_config in app_configs
        )
    errors = []
    for model in models:
        if len(model._meta.model_name) > 100:
            errors.append(
                Error(
                    "Model names must be at most 100 characters (got %d)."
                    % (len(model._meta.model_name),),
                    obj=model,
                    id="contenttypes.E005",
                )
            )
    return errors
