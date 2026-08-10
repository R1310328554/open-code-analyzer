#
#  Copyright 2024 The InfiniFlow Authors. All Rights Reserved.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
"""
热重载配置基类：子类定义类属性，运行时通过 get/get_all 读取。
"""

#
class ReloadConfigBase:
    # 非 callable、非私有类属性视为可重载配置项
    @classmethod
    def get_all(cls):
        # 返回 {配置名: 值} 字典
        configs = {}
        for k, v in cls.__dict__.items():
            if not callable(getattr(cls, k)) and not k.startswith("__") and not k.startswith("_"):
                configs[k] = v
        return configs

    @classmethod
    def get(cls, config_name):
        # 按名称读取单项，不存在返回 None
        return getattr(cls, config_name) if hasattr(cls, config_name) else None
