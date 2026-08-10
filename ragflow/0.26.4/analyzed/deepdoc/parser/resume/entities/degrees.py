#
#  Copyright 2025 The InfiniFlow Authors. All Rights Reserved.
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
#
"""
学历编码词典：ID 与中文/英文学历名称双向映射。
"""



# 招聘系统学历 ID -> 显示名
TBL = {
    "94": "EMBA",
    "6": "MBA",
    "95": "MPA",
    "92": "专升本",
    "4": "专科",
    "90": "中专",
    "91": "中技",
    "86": "初中",
    "3": "博士",
    "10": "博士后",
    "1": "本科",
    "2": "硕士",
    "87": "职高",
    "89": "高中",
}

# 显示名 -> ID 反向表
TBL_ = {v: k for k, v in TBL.items()}


def get_name(id):
    # 由学历 ID 取显示名
    return TBL.get(str(id), "")


def get_id(nm):
    # 由学历名称（大写）取 ID
    if not nm:
        return ""
    return TBL_.get(nm.upper().strip(), "")
