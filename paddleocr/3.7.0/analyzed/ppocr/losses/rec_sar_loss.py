from __future__ import absolute_import
from __future__ import division
from __future__ import print_function

# SAR 自回归识别损失：预测去末步、标签去首步后展平 CE
import paddle
from paddle import nn


    # Show-Attend-Read 损失：teacher forcing 逐步 CE，ignore_index 默认 92
class SARLoss(nn.Layer):
    def __init__(self, **kwargs):
        super(SARLoss, self).__init__()
        ignore_index = kwargs.get("ignore_index", 92)  # 6626
        self.loss_func = paddle.nn.loss.CrossEntropyLoss(
            reduction="mean", ignore_index=ignore_index
        )

    def forward(self, predicts, batch):
        predict = predicts[
            :, :-1, :
        ]  # 去掉输出末步，使序列长度与右移标签一致
        ]  # ignore last index of outputs to be in same seq_len with targets
        label = batch[1].astype("int64")[
            :, 1:
        ]  # 标签去掉首 token（通常为 SOS），与预测逐步对齐
        ]  # ignore first index of target in loss calculation
        batch_size, num_steps, num_classes = (
            predict.shape[0],
            predict.shape[1],
            predict.shape[2],
        )
        assert (
            len(label.shape) == len(list(predict.shape)) - 1
        ), "The target's shape and inputs's shape is [N, d] and [N, num_steps]"

        inputs = paddle.reshape(predict, [-1, num_classes])
        targets = paddle.reshape(label, [-1])
        loss = self.loss_func(inputs, targets)
        return {"loss": loss}
