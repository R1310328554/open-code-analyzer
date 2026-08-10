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
"""
通用字节/字符串转换与 xxhash128 摘要工具。
"""

#
import xxhash


def string_to_bytes(string):
    # str → utf-8 bytes（已是 bytes 则原样返回）
    return string if isinstance(string, bytes) else string.encode(encoding="utf-8")


def bytes_to_string(byte):
    # bytes → utf-8 字符串
    return byte.decode(encoding="utf-8")


# 128 位摘要 → 32 字符十六进制
def hash128(data: str) -> str:
    # 对字符串计算 xxhash128 十六进制摘要
    return xxhash.xxh128(data).hexdigest()
