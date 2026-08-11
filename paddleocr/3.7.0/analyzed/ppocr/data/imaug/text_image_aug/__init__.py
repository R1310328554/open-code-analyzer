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

# 文本图像增强（TIA）子包：导出透视、扭曲、拉伸三种识别增强函数
from .augment import tia_perspective, tia_distort, tia_stretch

# tia_* 供 RecAug 等识别流水线调用，模拟相机/扫描畸变
__all__ = ["tia_distort", "tia_stretch", "tia_perspective"]
