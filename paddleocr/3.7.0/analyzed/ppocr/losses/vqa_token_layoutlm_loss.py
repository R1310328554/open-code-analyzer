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

from paddle import nn
# VQA LayoutLM token 序列标注损失：仅 attention_mask=1 位置计算 CE
from ppocr.losses.basic_loss import DMLLoss


    # 文档 VQA SER：predicts 可经 key 提取，对 batch[5] 标签分类
class VQASerTokenLayoutLMLoss(nn.Layer):
    def __init__(self, num_classes, key=None):
        super().__init__()
        self.loss_class = nn.CrossEntropyLoss()
        self.num_classes = num_classes
        self.ignore_index = self.loss_class.ignore_index
        self.key = key

    def forward(self, predicts, batch):
        if isinstance(predicts, dict) and self.key is not None:
            predicts = predicts[self.key]
        labels = batch[5]
        attention_mask = batch[2]
        # 有 mask 时只监督有效 token，忽略 padding 与特殊占位
        if attention_mask is not None:
            active_loss = (
                attention_mask.reshape(
                    [
                        -1,
                    ]
                )
                == 1
            )
            active_output = predicts.reshape([-1, self.num_classes])[active_loss]
            active_label = labels.reshape(
                [
                    -1,
                ]
            )[active_loss]
            loss = self.loss_class(active_output, active_label)
        else:
            loss = self.loss_class(
                predicts.reshape([-1, self.num_classes]),
                labels.reshape(
                    [
                        -1,
                    ]
                ),
            )
        return {"loss": loss}
