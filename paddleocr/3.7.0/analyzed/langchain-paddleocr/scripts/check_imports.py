# Copyright (c) 2026 PaddlePaddle Authors. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# CI 辅助脚本：逐文件 SourceFileLoader 加载，捕获 import 语法/依赖错误
import sys
import traceback
from importlib.machinery import SourceFileLoader

# 用法：python check_imports.py file1.py file2.py；任一失败 exit 1
if __name__ == "__main__":
    files = sys.argv[1:]
    has_failure = False
    # 对每个待检文件尝试 load_module，失败则打印路径与 traceback
    for file in files:
        try:
            SourceFileLoader("x", file).load_module()
        except Exception:
            has_failure = True
            print(file)  # noqa: T201
            traceback.print_exc()
            print()  # noqa: T201

    sys.exit(1 if has_failure else 0)
