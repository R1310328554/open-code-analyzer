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
# 语义分割 IoU 损失辅助：在有效像素 mask 上逐类计算交并比
"""
This code is refer from:
https://github.com/whai362/PSENet/blob/python3/models/loss/iou.py
"""

import paddle

EPS = 1e-6


# 单样本逐类 IoU 求平均，空 mask 时交并均为 0
def iou_single(a, b, mask, n_class):
    valid = mask == 1
    a = a.masked_select(valid)
    b = b.masked_select(valid)
    miou = []
    for i in range(n_class):
        if a.shape == [0] and a.shape == b.shape:
            inter = paddle.to_tensor(0.0)
            union = paddle.to_tensor(0.0)
        else:
            inter = ((a == i).logical_and(b == i)).astype("float32")
            union = ((a == i).logical_or(b == i)).astype("float32")
        miou.append(paddle.sum(inter) / (paddle.sum(union) + EPS))
    miou = sum(miou) / len(miou)
    return miou


# 批量 IoU：展平后逐样本调用 iou_single，可选 batch 均值
def iou(a, b, mask, n_class=2, reduce=True):
    batch_size = a.shape[0]

    a = a.reshape([batch_size, -1])
    b = b.reshape([batch_size, -1])
    mask = mask.reshape([batch_size, -1])

    iou = paddle.zeros((batch_size,), dtype="float32")
    for i in range(batch_size):
        iou[i] = iou_single(a[i], b[i], mask[i], n_class)

    if reduce:
        iou = paddle.mean(iou)
    return iou
