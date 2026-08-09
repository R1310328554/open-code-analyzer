from __future__ import annotations

import os
import typing as t
from datetime import timedelta

from .cli import AppGroup
from .globals import current_app
from .helpers import send_from_directory
from .sansio.blueprints import Blueprint as SansioBlueprint
from .sansio.blueprints import BlueprintSetupState as BlueprintSetupState  # noqa
from .sansio.scaffold import _sentinel

if t.TYPE_CHECKING:  # pragma: no cover
    from .wrappers import Response


class Blueprint(SansioBlueprint):
    def __init__(
        self,
        name: str,
        import_name: str,
        static_folder: str | os.PathLike[str] | None = None,
        static_url_path: str | None = None,
        template_folder: str | os.PathLike[str] | None = None,
        url_prefix: str | None = None,
        subdomain: str | None = None,
        url_defaults: dict[str, t.Any] | None = None,
        root_path: str | None = None,
        cli_group: str | None = _sentinel,  # type: ignore
    ) -> None:
        super().__init__(
            name,
            import_name,
            static_folder,
            static_url_path,
            template_folder,
            url_prefix,
            subdomain,
            url_defaults,
            root_path,
            cli_group,
        )

        #: 用于为此对象注册 CLI 命令的 Click 命令组。
        #: 应用被发现且蓝图注册后，这些命令可通过 ``flask`` 命令使用。
        self.cli = AppGroup()

        # 设置 Click 组的名称，以便将应用命令挂载到其他 CLI 工具。
        self.cli.name = self.name

    def get_send_file_max_age(self, filename: str | None) -> int | None:
        """供 :func:`send_file` 在未显式传入 ``max_age`` 时，
        根据文件路径确定缓存 ``max_age`` 值。

        默认返回 :data:`~flask.current_app` 配置中的
        :data:`SEND_FILE_MAX_AGE_DEFAULT`，其默认值为 ``None``，
        表示让浏览器使用条件请求而非定时缓存，通常更为合适。

        注意：此方法与 :class:`Flask` 类中的同名方法重复。

        .. versionchanged:: 2.0
            默认配置由 12 小时改为 ``None``。

        .. versionadded:: 0.9
        """
        value = current_app.config["SEND_FILE_MAX_AGE_DEFAULT"]

        if value is None:
            return None

        if isinstance(value, timedelta):
            return int(value.total_seconds())

        return value  # type: ignore[no-any-return]

    def send_static_file(self, filename: str) -> Response:
        """用于从 :attr:`static_folder` 提供静态文件的视图函数。
        若设置了 :attr:`static_folder`，会自动在
        :attr:`static_url_path` 注册此视图的路由。

        注意：此方法与 :class:`Flask` 类中的同名方法重复。

        .. versionadded:: 0.5

        """
        if not self.has_static_folder:
            raise RuntimeError("'static_folder' must be set to serve static_files.")

        # send_file 只会调用应用上的 get_send_file_max_age，
        # 在此显式调用以使蓝图也能生效。
        max_age = self.get_send_file_max_age(filename)
        return send_from_directory(
            t.cast(str, self.static_folder), filename, max_age=max_age
        )

    def open_resource(
        self, resource: str, mode: str = "rb", encoding: str | None = "utf-8"
    ) -> t.IO[t.AnyStr]:
        """以只读方式打开相对于 :attr:`root_path` 的资源文件。
        相当于应用 :meth:`~.Flask.open_resource` 的蓝图版本。

        :param resource: 相对于 :attr:`root_path` 的资源路径。
        :param mode: 打开模式，仅支持读取，有效值为 ``"r"``（或 ``"rt"``）
            和 ``"rb"``。
        :param encoding: 以文本模式打开时使用的编码，二进制模式下忽略。

        .. versionchanged:: 3.1
            新增 ``encoding`` 参数。
        """
        if mode not in {"r", "rt", "rb"}:
            raise ValueError("Resources can only be opened for reading.")

        path = os.path.join(self.root_path, resource)

        if mode == "rb":
            return open(path, mode)  # pyright: ignore

        return open(path, mode, encoding=encoding)
