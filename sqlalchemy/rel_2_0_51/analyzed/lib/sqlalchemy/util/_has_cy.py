# util/_has_cy.py
# Copyright (C) 2005-2026 the SQLAlchemy authors and contributors
# <see AUTHORS file>
#
# This module is part of SQLAlchemy and is released under
# the MIT License: https://www.opensource.org/licenses/mit-license.php
# mypy: ignore-errors

# Cython 扩展探测：运行时检测 cyextension 是否可用
import osimport os
import typing


# 导入全部可选 Cython 扩展模块
def _import_cy_extensions():
    # all cython extension extension modules are treated as optional by the
    # setup, so to ensure that all are compiled, all should be imported here
    from ..cyextension import collections
    from ..cyextension import immutabledict
    from ..cyextension import processors
    from ..cyextension import resultproxy
    from ..cyextension import util

    return (collections, immutabledict, processors, resultproxy, util)


_CYEXTENSION_MSG: str
if not typing.TYPE_CHECKING:
    if os.environ.get("DISABLE_SQLALCHEMY_CEXT_RUNTIME"):
        HAS_CYEXTENSION = False
        _CYEXTENSION_MSG = "DISABLE_SQLALCHEMY_CEXT_RUNTIME is set"
    else:
        try:
            _import_cy_extensions()
        except ImportError as err:
            HAS_CYEXTENSION = False
            _CYEXTENSION_MSG = str(err)
        else:
            _CYEXTENSION_MSG = "Loaded"
            HAS_CYEXTENSION = True
else:
    HAS_CYEXTENSION = False
