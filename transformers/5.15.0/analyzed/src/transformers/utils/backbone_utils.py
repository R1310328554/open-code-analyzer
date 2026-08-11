# 骨干网络兼容层：BackboneConfigMixin/BackboneMixin 弃用转发
import warnings

from ..backbone_utils import BackboneConfigMixin, BackboneMixin


# BackboneConfigMixin：骨干配置 Mixin 弃用转发：提示从 backbone_utils 导入
class BackboneConfigMixin(BackboneConfigMixin):
    warnings.warn(
        "Importing `BackboneConfigMixin` from `utils/backbone_utils.py` is deprecated and will be removed in "
        "Transformers v5.10. Import as `from transformers.backbone_utils import BackboneConfigMixin` instead.",
        FutureWarning,
    )


# BackboneMixin：骨干模型 Mixin 弃用转发：提示从 backbone_utils 导入
class BackboneMixin(BackboneMixin):
    warnings.warn(
        "Importing `BackboneMixin` from `utils/backbone_utils.py` is deprecated and will be removed in "
        "Transformers v5.10. Import as `from transformers.backbone_utils import BackboneMixin` instead.",
        FutureWarning,
    )
