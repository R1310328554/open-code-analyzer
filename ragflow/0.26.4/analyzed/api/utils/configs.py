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
受限 pickle 序列化：Base64 编解码，仅允许 numpy/rag_flow 模块反序列化。
"""

#

import io
import base64
import pickle
from api.utils.common import bytes_to_string, string_to_bytes

safe_module = {"numpy", "rag_flow"}  # Unpickler 白名单模块前缀


class RestrictedUnpickler(pickle.Unpickler):
    # 重写 find_class，拒绝非白名单的全局类
    def find_class(self, module, name):
        import importlib

        if module.split(".")[0] in safe_module:
            _module = importlib.import_module(module)
            return getattr(_module, name)
        # 其余模块一律禁止反序列化
        raise pickle.UnpicklingError("global '%s.%s' is forbidden" % (module, name))


def restricted_loads(src):
    """安全版 pickle.loads，使用 RestrictedUnpickler。"""
    return RestrictedUnpickler(io.BytesIO(src)).load()


def serialize_b64(src, to_str=False):
    # pickle.dumps → base64；可选返回 str
    dest = base64.b64encode(pickle.dumps(src))
    if not to_str:
        return dest
    else:
        return bytes_to_string(dest)


def deserialize_b64(src):
    # base64 解码后 restricted_loads
    src = base64.b64decode(string_to_bytes(src) if isinstance(src, str) else src)
    return restricted_loads(src)
