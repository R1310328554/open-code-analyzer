from __future__ import annotations

import logging
import sys
import typing as t

from werkzeug.local import LocalProxy

from .globals import request

if t.TYPE_CHECKING:  # pragma: no cover
    from .sansio.app import App


@LocalProxy
def wsgi_errors_stream() -> t.TextIO:
    """为应用选择最合适的错误输出流。若有活动请求，则写入
    ``wsgi.errors``，否则使用 ``sys.stderr``。

    若自行配置 :class:`logging.StreamHandler`，可将此函数用作流。
    若使用文件或字典配置且无法直接导入，可引用
    ``ext://flask.logging.wsgi_errors_stream``。
    """
    if request:
        return request.environ["wsgi.errors"]  # type: ignore[no-any-return]

    return sys.stderr


def has_level_handler(logger: logging.Logger) -> bool:
    """检查日志链中是否存在能处理给定 logger
    :meth:`有效级别 <~logging.Logger.getEffectiveLevel>` 的 handler。
    """
    level = logger.getEffectiveLevel()
    current = logger

    while current:
        if any(handler.level <= level for handler in current.handlers):
            return True

        if not current.propagate:
            break

        current = current.parent  # type: ignore

    return False


#: 将日志消息写入 :func:`~flask.logging.wsgi_errors_stream`，
#: 格式为 ``[%(asctime)s] %(levelname)s in %(module)s: %(message)s``。
default_handler = logging.StreamHandler(wsgi_errors_stream)  # type: ignore
default_handler.setFormatter(
    logging.Formatter("[%(asctime)s] %(levelname)s in %(module)s: %(message)s")
)


def create_logger(app: App) -> logging.Logger:
    """获取 Flask 应用的 logger，并在需要时进行配置。

    logger 名称与 :attr:`app.import_name <flask.Flask.name>` 相同。

    当 :attr:`~flask.Flask.debug` 启用且未设置级别时，将 logger
    级别设为 :data:`logging.DEBUG`。

    若 logger 的有效级别没有对应 handler，则添加一个指向
    :func:`~flask.logging.wsgi_errors_stream` 的
    :class:`~logging.StreamHandler`，并使用基本格式。
    """
    logger = logging.getLogger(app.name)

    if app.debug and not logger.level:
        logger.setLevel(logging.DEBUG)

    if not has_level_handler(logger):
        logger.addHandler(default_handler)

    return logger
