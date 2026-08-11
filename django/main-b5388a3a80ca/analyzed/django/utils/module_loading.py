"""
django.utils.module_loading — 动态导入与 autodiscover。

import_string、cached_import 及 INSTALLED_APPS 模块扫描。
"""

import copyimport copy
import os
import sys
from importlib import import_module
from importlib.util import find_spec as importlib_find


# 导入模块，已加载且初始化完成则复用 sys.modules
def cached_import(module_path):
    # Check whether module is loaded and fully initialized.
    if not (
        (module := sys.modules.get(module_path))
        and (spec := getattr(module, "__spec__", None))
        and getattr(spec, "_initializing", False) is False
    ):
        module = import_module(module_path)
    return module


# 点分路径导入模块或属性/类
def import_string(dotted_path):
    """
    Import a dotted module path and return the module or attribute/class
    designated by the path. Raise ImportError if the import failed.
    """
    if "." not in dotted_path:
        return cached_import(dotted_path)
    else:
        # Force a consistent state by eagerly importing the entire dotted path.
        try:
            cached_import(dotted_path)
        except ImportError:
            pass

        module_path, class_name = dotted_path.rsplit(".", 1)
        module = cached_import(module_path)

        # Attempt getattr first, which will return any named objects
        # in a loaded module or a previously loaded submodule.
        try:
            import_target = getattr(module, class_name)
        except AttributeError:
            try:
                import_target = cached_import(dotted_path)
            except ImportError as err:
                raise ImportError(
                    'Module "%s" does not define a "%s" attribute/class'
                    % (module_path, class_name)
                ) from err
        return import_target


# 扫描 INSTALLED_APPS 下指定模块名
def autodiscover_modules(*args, **kwargs):
    """
    Auto-discover INSTALLED_APPS modules and fail silently when
    not present. This forces an import on them to register any admin bits they
    may want.

    You may provide a register_to keyword parameter as a way to access a
    registry. This register_to object must have a _registry instance variable
    to access it.
    """
    from django.apps import apps

    register_to = kwargs.get("register_to")
    for app_config in apps.get_app_configs():
        for module_to_search in args:
            # Attempt to import the app's module.
            try:
                if register_to:
                    before_import_registry = copy.copy(register_to._registry)

                import_module("%s.%s" % (app_config.name, module_to_search))
            except Exception:
                # Reset the registry to the state before the last import
                # as this import will have to reoccur on the next request and
                # this could raise NotRegistered and AlreadyRegistered
                # exceptions (see #8245).
                if register_to:
                    register_to._registry = before_import_registry

                # Decide whether to bubble up this error. If the app just
                # doesn't have the module in question, we can ignore the error
                # attempting to import it, otherwise we want it to bubble up.
                if module_has_submodule(app_config.module, module_to_search):
                    raise


# 包是否包含子模块（find_spec）
def module_has_submodule(package, module_name):
    """See if 'module' is in 'package'."""
    try:
        package_name = package.__name__
        package_path = package.__path__
    except AttributeError:
        # package isn't a package.
        return False

    full_module_name = package_name + "." + module_name
    try:
        return importlib_find(full_module_name, package_path) is not None
    except ModuleNotFoundError:
        # When module_name is an invalid dotted path, Python raises
        # ModuleNotFoundError.
        return False


# 返回模块所在目录路径
def module_dir(module):
    """
    Find the name of the directory that contains a module, if possible.

    Raise ValueError otherwise, e.g. for namespace packages that are split
    over several directories.
    """
    # Convert to list because __path__ may not support indexing.
    paths = list(getattr(module, "__path__", []))
    if len(paths) == 1:
        return paths[0]
    else:
        filename = getattr(module, "__file__", None)
        if filename is not None:
            return os.path.dirname(filename)
    raise ValueError("Cannot determine directory containing %s" % module)
