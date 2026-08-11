# copyright (c) 2021 PaddlePaddle Authors. All Rights Reserve.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

from __future__ import absolute_import
from __future__ import division
from __future__ import print_function

import paddle
from paddle import nn
# 表格注意力结构识别损失：HTML 结构 token CE + 单元格坐标回归
from paddle.nn import functional as F


    # 表格 Attention：structure CE（去首 token）+ 可选 loc MSE
class TableAttentionLoss(nn.Layer):
    def __init__(self, structure_weight=1.0, loc_weight=0.0, **kwargs):
        super(TableAttentionLoss, self).__init__()
        self.loss_func = nn.CrossEntropyLoss(weight=None, reduction="none")
        self.structure_weight = structure_weight
        self.loc_weight = loc_weight

    def forward(self, predicts, batch):
        structure_probs = predicts["structure_probs"]
        structure_targets = batch[1].astype("int64")
        structure_targets = structure_targets[:, 1:]
        structure_probs = paddle.reshape(
            structure_probs, [-1, structure_probs.shape[-1]]
        )
        structure_targets = paddle.reshape(structure_targets, [-1])
        structure_loss = self.loss_func(structure_probs, structure_targets)

        structure_loss = paddle.mean(structure_loss) * self.structure_weight

        loc_preds = predicts["loc_preds"]
        loc_targets = batch[2].astype("float32")
        loc_targets_mask = batch[3].astype("float32")
        loc_targets = loc_targets[:, 1:, :]
        loc_targets_mask = loc_targets_mask[:, 1:, :]
        loc_loss = (
            F.mse_loss(loc_preds * loc_targets_mask, loc_targets) * self.loc_weight
        )

        total_loss = structure_loss + loc_loss
        return {
            "loss": total_loss,
            "structure_loss": structure_loss,
            "loc_loss": loc_loss,
        }


    # 结构化位置注意力：按 max_len 裁剪序列，loc 用 SmoothL1 掩码归一
class SLALoss(nn.Layer):
    def __init__(self, structure_weight=1.0, loc_weight=0.0, loc_loss="mse", **kwargs):
        super(SLALoss, self).__init__()
        self.loss_func = nn.CrossEntropyLoss(weight=None, reduction="mean")
        self.structure_weight = structure_weight
        self.loc_weight = loc_weight
        self.loc_loss = loc_loss
        self.eps = 1e-12

    def forward(self, predicts, batch):
        structure_probs = predicts["structure_probs"]
        structure_targets = batch[1].astype("int64")
        max_len = batch[-2].max().astype("int32")
        structure_targets = structure_targets[:, 1 : max_len + 2]

        structure_loss = self.loss_func(structure_probs, structure_targets)

        structure_loss = paddle.mean(structure_loss) * self.structure_weight

        loc_preds = predicts["loc_preds"]
        loc_targets = batch[2].astype("float32")
        loc_targets_mask = batch[3].astype("float32")
        loc_targets = loc_targets[:, 1 : max_len + 2]
        loc_targets_mask = loc_targets_mask[:, 1 : max_len + 2]

        loc_loss = (
            F.smooth_l1_loss(
                loc_preds * loc_targets_mask,
                loc_targets * loc_targets_mask,
                reduction="sum",
            )
            * self.loc_weight
        )

        loc_loss = loc_loss / (loc_targets_mask.sum() + self.eps)
        total_loss = structure_loss + loc_loss
        return {
            "loss": total_loss,
            "structure_loss": structure_loss,
            "loc_loss": loc_loss,
        }
