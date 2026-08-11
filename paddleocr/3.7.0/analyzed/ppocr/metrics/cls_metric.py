# copyright (c) 2020 PaddlePaddle Authors. All Rights Reserve.
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


# 文本方向/角度分类评估：逐样本比对 pred 与 target 累计准确率
class ClsMetric(object):
    def __init__(self, main_indicator="acc", **kwargs):
        self.main_indicator = main_indicator
        self.eps = 1e-5
        self.reset()

    # 单 batch 统计正确数，返回即时 acc
    def __call__(self, pred_label, *args, **kwargs):
        preds, labels = pred_label
        correct_num = 0
        all_num = 0
        for (pred, pred_conf), (target, _) in zip(preds, labels):
            if pred == target:
                correct_num += 1
            all_num += 1
        self.correct_num += correct_num
        self.all_num += all_num
        return {
            "acc": correct_num / (all_num + self.eps),
        }

    # 汇总全局 acc 后 reset 计数器
    def get_metric(self):
        """
        return metrics {
                 'acc': 0
            }
        """
        acc = self.correct_num / (self.all_num + self.eps)
        self.reset()
        return {"acc": acc}

    # 清零 correct_num / all_num
    def reset(self):
        self.correct_num = 0
        self.all_num = 0
