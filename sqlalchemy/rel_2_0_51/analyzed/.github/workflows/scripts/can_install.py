"""
can_install — CI 辅助脚本：校验 wheel 标签是否与当前 Python 环境兼容。

读取 packaging.tags 并与命令行参数比对，不匹配时 exit(1)。
"""

import sys

from packaging import tags

# 默认待检标签；有 argv[1] 时取其前两段（如 cp312-cp312）
to_check = "--"
found = False
if len(sys.argv) > 1:
    to_check = sys.argv[1]
    # 遍历本机支持的 wheel tag，匹配则打印成功信息
    for t in tags.sys_tags():
        start = "-".join(str(t).split("-")[:2])
        if to_check.lower() == start:
            print(
                "Wheel tag {0} matches installed version {1}.".format(
                    to_check, t
                )
            )
            found = True
            break
# 未匹配任何 tag 时列出全部可用 tag 并以码 1 退出
if not found:
    print(
        "Wheel tag {0} not found in installed version tags {1}.".format(
            to_check, [str(t) for t in tags.sys_tags()]
        )
    )
    exit(1)
