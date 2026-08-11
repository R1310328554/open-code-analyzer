import functools
import os

from django.apps import apps
from django.conf import settings
from django.contrib.staticfiles import utils
from django.core.checks import Error, Warning
from django.core.exceptions import ImproperlyConfigured
from django.core.files.storage import FileSystemStorage, Storage, default_storage
from django.utils._os import safe_join
from django.utils.functional import LazyObject, empty
from django.utils.module_loading import import_string

# To keep track on which directories the finder has searched the static files.
# 记录 finder 已搜索过的静态文件目录
searched_locations = []


# 静态文件查找器基类 — 自定义 finder 需继承并实现 find/list
class BaseFinder:
    """
    A base file finder to be used for custom staticfiles finder classes.
    """

    # 校验 finder 配置；子类可重写
    # 校验 STATICFILES_DIRS 类型、前缀斜杠及与 STATIC_ROOT 冲突
    def check(self, **kwargs):
        raise NotImplementedError(
            "subclasses may provide a check() method to verify the finder is "
            "configured correctly."
        )

    # 按相对路径查找绝对路径；find_all 为 True 时返回全部匹配
    # 在各 extra location 中查找并记录 searched_locations
    # 在各应用 static 目录中查找匹配文件
    # 在本地 storage 中查找文件
    def find(self, path, find_all=False):
        """
        Given a relative file path, find an absolute file path.

        If the ``find_all`` parameter is False (default) return only the first
        found file path; if True, return a list of all found files paths.
        """
        raise NotImplementedError(
            "subclasses of BaseFinder must provide a find() method"
        )

    # 列出可收集的静态文件，返回 (相对路径, storage) 迭代器
    # 遍历各 location 下文件（跳过不存在的目录）
    # 遍历各应用 storage 中的文件
    # 列出 storage 中所有可收集文件
    def list(self, ignore_patterns):
        """
        Given an optional list of paths to ignore, return a two item iterable
        consisting of the relative path and storage instance.
        """
        raise NotImplementedError(
            "subclasses of BaseFinder must provide a list() method"
        )


# 基于 STATICFILES_DIRS 在额外目录中查找静态文件
class FileSystemFinder(BaseFinder):
    """
    A static files finder that uses the ``STATICFILES_DIRS`` setting
    to locate files.
    """

    # 解析 STATICFILES_DIRS，为每个根目录创建 FileSystemStorage
    # 为每个应用创建 static 目录的 storage（目录存在才加入）
    def __init__(self, app_names=None, *args, **kwargs):
        # List of locations with static files
        self.locations = []
        # Maps dir paths to an appropriate storage instance
        self.storages = {}
        for root in settings.STATICFILES_DIRS:
            if isinstance(root, (list, tuple)):
                prefix, root = root
            else:
                prefix = ""
            if (prefix, root) not in self.locations:
                self.locations.append((prefix, root))
        for prefix, root in self.locations:
            filesystem_storage = FileSystemStorage(location=root)
            filesystem_storage.prefix = prefix
            self.storages[root] = filesystem_storage
        super().__init__(*args, **kwargs)

    def check(self, **kwargs):
        errors = []
        if not isinstance(settings.STATICFILES_DIRS, (list, tuple)):
            errors.append(
                Error(
                    "The STATICFILES_DIRS setting is not a tuple or list.",
                    hint="Perhaps you forgot a trailing comma?",
                    id="staticfiles.E001",
                )
            )
            return errors
        for root in settings.STATICFILES_DIRS:
            if isinstance(root, (list, tuple)):
                prefix, root = root
                if prefix.endswith("/"):
                    errors.append(
                        Error(
                            "The prefix %r in the STATICFILES_DIRS setting must "
                            "not end with a slash." % prefix,
                            id="staticfiles.E003",
                        )
                    )
            if settings.STATIC_ROOT and os.path.abspath(
                settings.STATIC_ROOT
            ) == os.path.abspath(root):
                errors.append(
                    Error(
                        "The STATICFILES_DIRS setting should not contain the "
                        "STATIC_ROOT setting.",
                        id="staticfiles.E002",
                    )
                )
            if not os.path.isdir(root):
                errors.append(
                    Warning(
                        f"The directory '{root}' in the STATICFILES_DIRS setting "
                        f"does not exist.",
                        id="staticfiles.W004",
                    )
                )
        return errors

    def find(self, path, find_all=False):
        """
        Look for files in the extra locations as defined in STATICFILES_DIRS.
        """
        matches = []
        for prefix, root in self.locations:
            if root not in searched_locations:
                searched_locations.append(root)
            matched_path = self.find_location(root, path, prefix)
            if matched_path:
                if not find_all:
                    return matched_path
                matches.append(matched_path)
        return matches

    # 在指定根目录与前缀下解析路径并返回存在的绝对路径
    def find_location(self, root, path, prefix=None):
        """
        Find a requested static file in a location and return the found
        absolute path (or ``None`` if no match).
        """
        if prefix:
            prefix = "%s%s" % (prefix, os.sep)
            if not path.startswith(prefix):
                return None
            path = path.removeprefix(prefix)
        path = safe_join(root, path)
        if os.path.exists(path):
            return path

    def list(self, ignore_patterns):
        """
        List all files in all locations.
        """
        for prefix, root in self.locations:
            # Skip nonexistent directories.
            if os.path.isdir(root):
                storage = self.storages[root]
                for path in utils.get_files(storage, ignore_patterns):
                    yield path, storage


# 在各已安装应用的 static/ 子目录中查找静态文件
class AppDirectoriesFinder(BaseFinder):
    """
    A static files finder that looks in the directory of each app as
    specified in the source_dir attribute.
    """

    storage_class = FileSystemStorage
    source_dir = "static"

    def __init__(self, app_names=None, *args, **kwargs):
        # The list of apps that are handled
        self.apps = []
        # Mapping of app names to storage instances
        self.storages = {}
        app_configs = apps.get_app_configs()
        if app_names:
            app_names = set(app_names)
            app_configs = [ac for ac in app_configs if ac.name in app_names]
        for app_config in app_configs:
            app_storage = self.storage_class(
                os.path.join(app_config.path, self.source_dir)
            )
            if os.path.isdir(app_storage.location):
                self.storages[app_config.name] = app_storage
                if app_config.name not in self.apps:
                    self.apps.append(app_config.name)
        super().__init__(*args, **kwargs)

    def list(self, ignore_patterns):
        """
        List all files in all app storages.
        """
        for storage in self.storages.values():
            if storage.exists(""):  # check if storage location exists
                for path in utils.get_files(storage, ignore_patterns):
                    yield path, storage

    def find(self, path, find_all=False):
        """
        Look for files in the app directories.
        """
        matches = []
        for app in self.apps:
            app_location = self.storages[app].location
            if app_location not in searched_locations:
                searched_locations.append(app_location)
            match = self.find_in_app(app, path)
            if match:
                if not find_all:
                    return match
                matches.append(match)
        return matches

    # 在指定应用的 storage 中查找单个路径
    def find_in_app(self, app, path):
        """
        Find a requested static file in an app's static locations.
        """
        storage = self.storages.get(app)
        # Only try to find a file if the source dir actually exists.
        if storage and storage.exists(path):
            matched_path = storage.path(path)
            if matched_path:
                return matched_path


# 基于 storage 后端的查找器基类 — 需指定 storage 类
class BaseStorageFinder(BaseFinder):
    """
    A base static files finder to be used to extended
    with an own storage class.
    """

    storage = None

    # 实例化 storage 并校验已配置
    def __init__(self, storage=None, *args, **kwargs):
        if storage is not None:
            self.storage = storage
        if self.storage is None:
            raise ImproperlyConfigured(
                "The staticfiles storage finder %r "
                "doesn't have a storage class "
                "assigned." % self.__class__
            )
        # Make sure we have a storage instance here.
        if not isinstance(self.storage, (Storage, LazyObject)):
            self.storage = self.storage()
        super().__init__(*args, **kwargs)

    def find(self, path, find_all=False):
        """
        Look for files in the default file storage, if it's local.
        """
        try:
            self.storage.path("")
        except NotImplementedError:
            pass
        else:
            if self.storage.location not in searched_locations:
                searched_locations.append(self.storage.location)
            if self.storage.exists(path):
                match = self.storage.path(path)
                if find_all:
                    match = [match]
                return match
        return []

    def list(self, ignore_patterns):
        """
        List all files of the storage.
        """
        for path in utils.get_files(self.storage, ignore_patterns):
            yield path, self.storage


# 使用 default_storage 作为查找后端的 finder
class DefaultStorageFinder(BaseStorageFinder):
    """
    A static files finder that uses the default storage backend.
    """

    storage = default_storage

    # 校验 default_storage 具有有效 base_location
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        base_location = getattr(self.storage, "base_location", empty)
        if not base_location:
            raise ImproperlyConfigured(
                "The storage backend of the "
                "staticfiles finder %r doesn't have "
                "a valid location." % self.__class__
            )


# 调用所有已启用 finder 查找静态文件
def find(path, find_all=False):
    """
    Find a static file with the given path using all enabled finders.

    If ``find_all`` is ``False`` (default), return the first matching
    absolute path (or ``None`` if no match). Otherwise return a list.
    """
    searched_locations[:] = []
    matches = []
    for finder in get_finders():
        result = finder.find(path, find_all=find_all)
        if not find_all and result:
            return result
        if not isinstance(result, (list, tuple)):
            result = [result]
        matches.extend(result)
    if matches:
        return matches
    # No match.
    return [] if find_all else None


# 按 STATICFILES_FINDERS 设置逐个 yield finder 实例
def get_finders():
    for finder_path in settings.STATICFILES_FINDERS:
        yield get_finder(finder_path)


# 导入并实例化 finder 类，校验其为 BaseFinder 子类
@functools.cache
def get_finder(import_path):
    """
    Import the staticfiles finder class described by import_path, where
    import_path is the full Python path to the class.
    """
    Finder = import_string(import_path)
    if not issubclass(Finder, BaseFinder):
        raise ImproperlyConfigured(
            'Finder "%s" is not a subclass of "%s"' % (Finder, BaseFinder)
        )
    return Finder()
