# FPGM 结构化剪枝：对 Conv 权重按 FPGM 准则裁剪指定比例
import paddleslim
import paddle
import numpy as np

from paddleslim.dygraph import FPGMFilterPruner


# 用 FPGMFilterPruner 对非 linear/transpose 参数按 prune_ratio 剪枝
def prune_model(model, input_shape, prune_ratio=0.1):
    flops = paddle.flops(model, input_shape)
    pruner = FPGMFilterPruner(model, input_shape)

    params_sensitive = {}
    for param in model.parameters():
        if "transpose" not in param.name and "linear" not in param.name:
            # set prune ratio as 10%. The larger the value, the more convolution weights will be cropped
            params_sensitive[param.name] = prune_ratio

    plan = pruner.prune_vars(params_sensitive, [0])

    flops = paddle.flops(model, input_shape)
    return model
