from typing import TYPE_CHECKING

from ...utils import _LazyModule
from ...utils.import_utils import define_import_structure


if TYPE_CHECKING:
    from .configuration_radio import *
    from .modeling_radio import *
else:
    import sys

    # 运行时以 _LazyModule 延迟加载子模块，避免循环导入并加快包导入速度
    _file = globals()["__file__"]
    sys.modules[__name__] = _LazyModule(__name__, _file, define_import_structure(_file), module_spec=__spec__)
