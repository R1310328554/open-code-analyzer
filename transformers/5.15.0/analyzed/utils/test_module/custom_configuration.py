# 自定义配置类：用于 dynamic module 加载测试的最小 PreTrainedConfig 子类
from transformers import PreTrainedConfig


# CustomConfig：model_type 为 custom 的简单配置，含 attribute 字段
class CustomConfig(PreTrainedConfig):
    model_type = "custom"

    def __init__(self, attribute=1, **kwargs):
        self.attribute = attribute
        super().__init__(**kwargs)
