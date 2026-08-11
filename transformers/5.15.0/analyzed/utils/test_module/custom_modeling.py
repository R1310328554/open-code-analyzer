# 自定义模型：最小 PreTrainedModel 实现，含 linear 层与 forward
import torch

from transformers import PreTrainedModel

from .custom_configuration import CustomConfig


# CustomModel：绑定 CustomConfig 的线性层模型，用于 remote code 测试
class CustomModel(PreTrainedModel):
    config_class = CustomConfig

    def __init__(self, config):
        super().__init__(config)
        self.linear = torch.nn.Linear(config.hidden_size, config.hidden_size)
        self.post_init()

    # forward：对输入张量执行线性变换
    def forward(self, x):
        return self.linear(x)

    # _init_weights：权重初始化钩子（测试用空实现）
    def _init_weights(self, module):
        pass
