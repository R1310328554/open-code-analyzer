from __future__ import annotations

import errno
import json
import os
import types
import typing as t

from werkzeug.utils import import_string

if t.TYPE_CHECKING:
    import typing_extensions as te

    from .sansio.app import App


T = t.TypeVar("T")


class ConfigAttribute(t.Generic[T]):
    """使属性转发到 config 的描述符。"""

    def __init__(
        self, name: str, get_converter: t.Callable[[t.Any], T] | None = None
    ) -> None:
        self.__name__ = name
        self.get_converter = get_converter

    @t.overload
    def __get__(self, obj: None, owner: None) -> te.Self: ...

    @t.overload
    def __get__(self, obj: App, owner: type[App]) -> T: ...

    def __get__(self, obj: App | None, owner: type[App] | None = None) -> T | te.Self:
        if obj is None:
            return self

        rv = obj.config[self.__name__]

        if self.get_converter is not None:
            rv = self.get_converter(rv)

        return rv  # type: ignore[no-any-return]

    def __set__(self, obj: App, value: t.Any) -> None:
        obj.config[self.__name__] = value


class Config(dict):  # type: ignore[type-arg]
    """行为与 dict 完全相同，但提供从文件或特殊字典填充配置的方式。
    填充配置有两种常见模式。

    可从配置文件填充::

        app.config.from_pyfile('yourconfig.cfg')

    也可在调用 :meth:`from_object` 的模块中定义配置项，或提供要加载的
    模块导入路径。还可使用同一模块，在调用前直接提供配置值::

        DEBUG = True
        SECRET_KEY = 'development key'
        app.config.from_object(__name__)

    两种情况下（从 Python 文件或模块加载），仅大写键会加入配置。
    这样可在配置文件中使用小写值作为临时变量，或在实现应用的同一文件中
    定义配置键。

    从指向文件的环境变量加载配置可能是最实用的方式::

        app.config.from_envvar('YOURAPPLICATION_SETTINGS')

    启动应用前须将此环境变量设为配置文件路径。Linux 和 macOS 使用 export::

        export YOURAPPLICATION_SETTINGS='/path/to/config/file'

    Windows 使用 `set`。

    :param root_path: 读取文件时的相对根路径。由应用创建 config 对象时，
                      此为应用的 :attr:`~flask.Flask.root_path`。
    :param defaults: 可选的默认值字典。
    """

    def __init__(
        self,
        root_path: str | os.PathLike[str],
        defaults: dict[str, t.Any] | None = None,
    ) -> None:
        super().__init__(defaults or {})
        self.root_path = root_path

    def from_envvar(self, variable_name: str, silent: bool = False) -> bool:
        """从指向配置文件的环境变量加载配置。本质上是以下代码的快捷方式，
        并提供更友好的错误信息::

            app.config.from_pyfile(os.environ['YOURAPPLICATION_SETTINGS'])

        :param variable_name: 环境变量名。
        :param silent: 文件缺失时设为 ``True`` 以静默失败。
        :return: 文件加载成功时返回 ``True``。
        """
        rv = os.environ.get(variable_name)
        if not rv:
            if silent:
                return False
            raise RuntimeError(
                f"The environment variable {variable_name!r} is not set"
                " and as such configuration could not be loaded. Set"
                " this variable and make it point to a configuration"
                " file"
            )
        return self.from_pyfile(rv, silent=silent)

    def from_prefixed_env(
        self, prefix: str = "FLASK", *, loads: t.Callable[[str], t.Any] = json.loads
    ) -> bool:
        """加载所有以 ``FLASK_`` 开头的环境变量，从键名中去掉前缀作为配置键。
        值经加载函数尝试转换为比字符串更具体的类型。

        键按 :func:`sorted` 顺序加载。

        默认加载函数尝试将值解析为任意有效 JSON 类型，包括 dict 和 list。

        嵌套 dict 中的特定项可用双下划线（``__``）分隔键名设置。
        若中间键不存在，将初始化为空 dict。

        :param prefix: 加载此前缀开头的环境变量，以下划线（``_``）分隔。
        :param loads: 将每个字符串值传入此函数，返回值作为配置值。
            若抛出任何异常则忽略，值保持为字符串。默认为 :func:`json.loads`。

        .. versionadded:: 2.1
        """
        prefix = f"{prefix}_"

        for key in sorted(os.environ):
            if not key.startswith(prefix):
                continue

            value = os.environ[key]
            key = key.removeprefix(prefix)

            try:
                value = loads(value)
            except Exception:
                # 加载失败时保持字符串值。
                pass

            if "__" not in key:
                # 非嵌套键，直接设置。
                self[key] = value
                continue

            # 用 "__" 分隔的键遍历嵌套字典。
            current = self
            *parts, tail = key.split("__")

            for part in parts:
                # 中间 dict 不存在则创建。
                if part not in current:
                    current[part] = {}

                current = current[part]

            current[tail] = value

        return True

    def from_pyfile(
        self, filename: str | os.PathLike[str], silent: bool = False
    ) -> bool:
        """从 Python 文件更新配置值。行为等同于用 :meth:`from_object`
        导入该文件为模块。

        :param filename: 配置文件名，可为绝对路径或相对于 root_path 的路径。
        :param silent: 文件缺失时设为 ``True`` 以静默失败。
        :return: 文件加载成功时返回 ``True``。

        .. versionadded:: 0.7
           新增 ``silent`` 参数。
        """
        filename = os.path.join(self.root_path, filename)
        d = types.ModuleType("config")
        d.__file__ = filename
        try:
            with open(filename, mode="rb") as config_file:
                exec(compile(config_file.read(), filename, "exec"), d.__dict__)
        except OSError as e:
            if silent and e.errno in (errno.ENOENT, errno.EISDIR, errno.ENOTDIR):
                return False
            e.strerror = f"Unable to load configuration file ({e.strerror})"
            raise
        self.from_object(d)
        return True

    def from_object(self, obj: object | str) -> None:
        """从给定对象更新配置值。对象可为以下两种类型之一：

        -   字符串：导入该名称的对象
        -   实际对象引用：直接使用该对象

        对象通常为模块或类。:meth:`from_object` 仅加载模块/类的大写属性。
        ``dict`` 对象无法与 :meth:`from_object` 配合使用，因为 dict 的键
        不是 dict 类的属性。

        基于模块的配置示例::

            app.config.from_object('yourapplication.default_config')
            from yourapplication import default_config
            app.config.from_object(default_config)

        加载前不对对象做任何处理。若对象是类且含 ``@property`` 属性，
        须先实例化再传入此方法。

        不应使用此函数加载实际配置，而应加载配置默认值。实际配置宜通过
        :meth:`from_pyfile` 加载，且最好来自包外路径，因为包可能已系统级安装。

        基于类的配置示例参见 :ref:`config-dev-prod`。

        :param obj: 导入名或对象。
        """
        if isinstance(obj, str):
            obj = import_string(obj)
        for key in dir(obj):
            if key.isupper():
                self[key] = getattr(obj, key)

    def from_file(
        self,
        filename: str | os.PathLike[str],
        load: t.Callable[[t.IO[t.Any]], t.Mapping[str, t.Any]],
        silent: bool = False,
        text: bool = True,
    ) -> bool:
        """使用 ``load`` 参数加载文件并更新配置值。加载的数据传递给
        :meth:`from_mapping` 方法。

        .. code-block:: python

            import json
            app.config.from_file("config.json", load=json.load)

            import tomllib
            app.config.from_file("config.toml", load=tomllib.load, text=False)

        :param filename: 数据文件路径，可为绝对路径或相对于配置根路径。
        :param load: 接收文件句柄并返回从文件加载的映射的可调用对象。
        :type load: ``Callable[[Reader], Mapping]``，其中 ``Reader`` 实现 ``read`` 方法。
        :param silent: 文件不存在时忽略。
        :param text: 以文本或二进制模式打开文件。
        :return: 文件加载成功时返回 ``True``。

        .. versionchanged:: 2.3
            新增 ``text`` 参数。

        .. versionadded:: 2.0
        """
        filename = os.path.join(self.root_path, filename)

        try:
            with open(filename, "r" if text else "rb") as f:
                obj = load(f)
        except OSError as e:
            if silent and e.errno in (errno.ENOENT, errno.EISDIR):
                return False

            e.strerror = f"Unable to load configuration file ({e.strerror})"
            raise

        return self.from_mapping(obj)

    def from_mapping(
        self, mapping: t.Mapping[str, t.Any] | None = None, **kwargs: t.Any
    ) -> bool:
        """类似 :meth:`update`，但忽略非大写键的项。

        :return: 始终返回 ``True``。

        .. versionadded:: 0.11
        """
        mappings: dict[str, t.Any] = {}
        if mapping is not None:
            mappings.update(mapping)
        mappings.update(kwargs)
        for key, value in mappings.items():
            if key.isupper():
                self[key] = value
        return True

    def get_namespace(
        self, namespace: str, lowercase: bool = True, trim_namespace: bool = True
    ) -> dict[str, t.Any]:
        """返回匹配指定命名空间/前缀的配置子集字典。用法示例::

            app.config['IMAGE_STORE_TYPE'] = 'fs'
            app.config['IMAGE_STORE_PATH'] = '/var/app/images'
            app.config['IMAGE_STORE_BASE_URL'] = 'http://img.website.com'
            image_store_config = app.config.get_namespace('IMAGE_STORE_')

        结果字典 `image_store_config` 形如::

            {
                'type': 'fs',
                'path': '/var/app/images',
                'base_url': 'http://img.website.com'
            }

        当配置项直接映射到函数或类构造器的关键字参数时很有用。

        :param namespace: 配置命名空间前缀。
        :param lowercase: 结果字典的键是否转为小写。
        :param trim_namespace: 结果字典的键是否去掉命名空间前缀。

        .. versionadded:: 0.11
        """
        rv = {}
        for k, v in self.items():
            if not k.startswith(namespace):
                continue
            if trim_namespace:
                key = k[len(namespace) :]
            else:
                key = k
            if lowercase:
                key = key.lower()
            rv[key] = v
        return rv

    def __repr__(self) -> str:
        return f"<{type(self).__name__} {dict.__repr__(self)}>"
